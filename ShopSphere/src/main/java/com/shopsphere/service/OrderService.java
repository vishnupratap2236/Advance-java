package com.shopsphere.service;

import com.shopsphere.dao.CartDao;
import com.shopsphere.dao.CouponDao;
import com.shopsphere.dao.OrderDao;
import com.shopsphere.dao.PaymentDao;
import com.shopsphere.dao.ProductDao;
import com.shopsphere.db.Database;
import com.shopsphere.db.Transactions;
import com.shopsphere.model.Address;
import com.shopsphere.model.CartItem;
import com.shopsphere.model.Coupon;
import com.shopsphere.model.Order;
import com.shopsphere.model.OrderItem;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.OrderStatusEvent;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Payment;
import com.shopsphere.model.Product;
import com.shopsphere.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Checkout and order lifecycle.
 *
 * <p>Placing an order runs in a single transaction: stock is re-checked and
 * decremented with a conditional update, the order and its lines are written and the
 * cart is cleared. If any step fails, everything is rolled back, so stock can never
 * be oversold.
 */
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class.getName());

    private final OrderDao orderDao = new OrderDao();
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();
    private final CouponDao couponDao = new CouponDao();
    private final PaymentDao paymentDao = new PaymentDao();

    /** Signals a business rule violation that must be shown to the customer. */
    public static class CheckoutException extends Exception {
        public CheckoutException(String message) {
            super(message);
        }
    }

    /** Request data for placing an order. */
    public record CheckoutRequest(long userId, String recipientName, String phone, String line1,
                                  String line2, String city, String state, String postalCode,
                                  String country, String notes, String paymentMethod,
                                  String couponCode) {
    }

    public Order placeOrder(CheckoutRequest request) throws SQLException, CheckoutException {
        List<CartItem> items = cartDao.findItems(request.userId());
        if (items.isEmpty()) {
            throw new CheckoutException("Your cart is empty.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product == null || !product.isActive()) {
                throw new CheckoutException(product == null
                        ? "A product in your cart is no longer available."
                        : product.getName() + " is no longer available.");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new CheckoutException("Only " + product.getStock() + " left of "
                        + product.getName() + ".");
            }
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        subtotal = Money.scale(subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Optional<Coupon> found = couponDao.findByCode(request.couponCode());
            if (found.isEmpty()) {
                throw new CheckoutException("The coupon code is not valid.");
            }
            coupon = found.get();
            if (!isCouponUsable(coupon, subtotal, request.userId())) {
                throw new CheckoutException("The coupon code cannot be used for this order.");
            }
            discount = Money.scale(coupon.discountFor(subtotal));
        }

        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal shipping = taxable.compareTo(
                com.shopsphere.config.AppConfig.get().freeShippingThreshold()) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : Money.scale(com.shopsphere.config.AppConfig.get().shippingFee());
        BigDecimal tax = taxable.multiply(
                com.shopsphere.config.AppConfig.get().taxRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = Money.scale(taxable.add(shipping).add(tax));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shipping);
        order.setTax(tax);
        order.setTotal(total);
        order.setCouponCode(coupon == null ? null : coupon.getCode());
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus(request.paymentMethod().equals("COD") ? "PENDING" : "PAID");
        order.setRecipientName(request.recipientName());
        order.setPhone(request.phone());
        order.setAddressLine1(request.line1());
        order.setAddressLine2(request.line2());
        order.setCity(request.city());
        order.setState(request.state());
        order.setPostalCode(request.postalCode());
        order.setCountry(request.country());
        order.setNotes(request.notes());

        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProduct().getName());
            orderItem.setProductSku(item.getProduct().getSku());
            orderItem.setImageUrl(item.getProduct().getImageUrl());
            orderItem.setUnitPrice(item.getProduct().getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setLineTotal(Money.scale(item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))));
            order.getItems().add(orderItem);
        }

        final Coupon appliedCoupon = coupon;
        Order saved = Transactions.inTransaction(connection -> {
            // Re-check and decrement the stock atomically. The WHERE clause makes the
            // update fail when another customer bought the last unit in the meantime.
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() == null) {
                    continue;
                }
                String sql = "UPDATE products SET stock = stock - ? "
                        + "WHERE id = ? AND active = 1 AND stock >= ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, item.getQuantity());
                    ps.setLong(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("STOCK_CONFLICT:" + item.getProductName());
                    }
                }
            }

            orderDao.insert(connection, order);
            orderDao.addStatusHistory(connection, order.getId(), OrderStatus.PENDING,
                    "Order placed", request.userId());

            // A payment record is written for every order. This project uses a demo
            // payment, so the transaction id is clearly marked as simulated.
            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setPaymentMethod(order.getPaymentMethod());
            payment.setAmount(order.getTotal());
            payment.setPaymentStatus(order.getPaymentStatus());
            if ("PENDING".equals(order.getPaymentStatus())) {
                payment.setTransactionId(null);
                payment.setGatewayResponse("Demo checkout: no payment provider was contacted.");
            } else {
                payment.setTransactionId("DEMO-" + order.getOrderNumber());
                payment.setGatewayResponse("Simulated payment accepted by the demo gateway.");
                payment.setPaidAt(LocalDateTime.now());
            }
            paymentDao.insert(connection, payment);

            if (appliedCoupon != null) {
                couponDao.incrementUsage(connection, appliedCoupon.getId());
                couponDao.recordUsage(connection, appliedCoupon.getId(), request.userId(),
                        order.getId(), order.getDiscount());
            }
            cartDao.clear(request.userId());
            return order;
        });

        LOG.info("Order " + saved.getOrderNumber() + " placed by user " + request.userId());
        syncInventory(saved);
        return saved;
    }

    /**
     * Informs the inventory service about the new stock levels.
     *
     * <p>The database transaction above is the authority; this only keeps a remote
     * inventory service (socket or RMI) in sync. Failures are logged and ignored.
     */
    private void syncInventory(Order order) {
        com.shopsphere.inventory.InventoryService inventory =
                com.shopsphere.inventory.InventoryServiceProvider.get();
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                continue;
            }
            try {
                int stock = productDao.findById(item.getProductId(), true)
                        .map(com.shopsphere.model.Product::getStock)
                        .orElse(0);
                inventory.setStock(item.getProductId(), stock);
            } catch (SQLException e) {
                LOG.log(java.util.logging.Level.WARNING,
                        "Could not synchronise the inventory of product " + item.getProductId(), e);
            }
        }
    }

    private boolean isCouponUsable(Coupon coupon, BigDecimal subtotal, long userId) {
        if (!coupon.isActive()) {
            return false;
        }
        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            return false;
        }
        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            return false;
        }
        if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return false;
        }
        try {
            return couponDao.countUsageByUser(coupon.getId(), userId) < coupon.getUsagePerUser();
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.WARNING, "Could not read the coupon usage", e);
            return false;
        }
    }

    /** Generates a readable, unique order number, e.g. {@code SS-20261020-4821}. */
    public String generateOrderNumber() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "SS-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter
                    .ofPattern("yyyyMMdd"))
                    + "-" + (1000 + (int) (Math.random() * 9000));
            try {
                if (orderDao.findByOrderNumber(candidate).isEmpty()) {
                    return candidate;
                }
            } catch (SQLException e) {
                LOG.log(java.util.logging.Level.WARNING, "Could not verify order number", e);
            }
        }
        return "SS-" + System.currentTimeMillis();
    }

    /** Returns an order of a customer, enforcing that the customer owns it. */
    public Optional<Order> findForUser(long orderId, long userId) throws SQLException {
        Optional<Order> order = orderDao.findById(orderId);
        if (order.isEmpty() || order.get().getUserId() != userId) {
            return Optional.empty();
        }
        return order;
    }

    public PageResult<Order> listForUser(long userId, int page, int pageSize) throws SQLException {
        return orderDao.findByUser(userId, page, pageSize);
    }

    public PageResult<Order> listAll(int page, int pageSize, String status, String search)
            throws SQLException {
        return orderDao.findAll(page, pageSize, status, search);
    }

    public Optional<Order> findById(long orderId) throws SQLException {
        return orderDao.findById(orderId);
    }

    /**
     * Updates the status of an order. Cancelling an order returns the reserved stock.
     */
    public boolean updateStatus(long orderId, OrderStatus status, String note, Long changedByUserId)
            throws SQLException {
        Order order = orderDao.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        Transactions.inTransaction(connection -> {
            if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProductId() == null) {
                        continue;
                    }
                    try (PreparedStatement ps = connection.prepareStatement(
                            "UPDATE products SET stock = stock + ? WHERE id = ?")) {
                        ps.setInt(1, item.getQuantity());
                        ps.setLong(2, item.getProductId());
                        ps.executeUpdate();
                    }
                }
            }
            orderDao.updateStatus(connection, orderId, status, note, changedByUserId);
            return null;
        });
        return true;
    }

    /** Lets a customer cancel their own order while it is still cancellable. */
    public boolean cancelByCustomer(long orderId, long userId) throws SQLException {
        Order order = orderDao.findById(orderId).orElse(null);
        if (order == null || order.getUserId() != userId || !order.getStatus().isCancellable()) {
            return false;
        }
        return updateStatus(orderId, OrderStatus.CANCELLED, "Cancelled by the customer", userId);
    }

    /** Stock levels of the products of an order, used by the administration screens. */
    public List<Product> productsOf(Order order) {
        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                continue;
            }
            try {
                productDao.findById(item.getProductId(), true).ifPresent(products::add);
            } catch (SQLException e) {
                LOG.log(java.util.logging.Level.WARNING, "Could not load product of an order", e);
            }
        }
        return products;
    }

    /** Counts orders per status, used for the dashboard badges. */
    public long countByStatus(OrderStatus status) throws SQLException {
        return orderDao.countByStatus(status);
    }

    public long countAll() throws SQLException {
        return orderDao.countAll();
    }

    public BigDecimal revenue() throws SQLException {
        return orderDao.revenue();
    }

    public List<Order> recentOrders(int limit) throws SQLException {
        return orderDao.recentOrders(limit);
    }

    public List<Order> topOrders(int limit) throws SQLException {
        return orderDao.topOrders(limit);
    }

    /** Timeline of an order, exposed for the order detail view. */
    public List<OrderStatusEvent> history(long orderId) throws SQLException {
        try (Connection connection = Database.connection()) {
            return orderDao.findHistory(connection, orderId);
        }
    }
}
