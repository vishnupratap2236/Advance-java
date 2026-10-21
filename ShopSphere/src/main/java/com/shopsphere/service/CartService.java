package com.shopsphere.service;

import com.shopsphere.dao.CartDao;
import com.shopsphere.dao.CouponDao;
import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.CartItem;
import com.shopsphere.model.CartView;
import com.shopsphere.model.Coupon;
import com.shopsphere.model.Product;
import com.shopsphere.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Cart operations and the server side calculation of all cart totals.
 *
 * <p>Prices are never taken from the browser: the subtotal is always recalculated
 * from the current product price in the database.
 */
public class CartService {

    private static final Logger LOG = Logger.getLogger(CartService.class.getName());
    private static final int MAX_QUANTITY_PER_ITEM = 20;

    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();
    private final CouponDao couponDao = new CouponDao();

    public CartView view(long userId, String couponCode) throws SQLException {
        List<CartItem> items = cartDao.findItems(userId);
        CartView view = new CartView();
        view.setItems(items);

        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            count += item.getQuantity();
            if (!product.isActive()) {
                view.getWarnings().add(product.getName() + " is no longer available and will be "
                        + "removed at checkout.");
            } else if (product.getStock() <= 0) {
                view.getWarnings().add(product.getName() + " is out of stock.");
            } else if (item.getQuantity() > product.getStock()) {
                view.getWarnings().add("Only " + product.getStock() + " left of "
                        + product.getName() + ".");
            }
        }
        view.setSubtotal(Money.scale(subtotal));
        view.setItemCount(count);

        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            Optional<Coupon> coupon = couponDao.findByCode(couponCode);
            if (coupon.isPresent() && isUsable(coupon.get(), subtotal, userId)) {
                discount = Money.scale(coupon.get().discountFor(subtotal));
                view.setCouponCode(coupon.get().getCode());
            } else {
                view.setCouponMessage("The coupon code is not valid for this order.");
            }
        }
        view.setDiscount(discount);

        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal shipping = calculateShipping(taxable);
        BigDecimal tax = taxable.multiply(taxRate()).setScale(2, RoundingMode.HALF_UP);
        view.setShippingFee(shipping);
        view.setTax(tax);
        view.setTotal(Money.scale(taxable.add(shipping).add(tax)));
        return view;
    }

    private BigDecimal calculateShipping(BigDecimal taxableAmount) {
        if (taxableAmount.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal threshold = com.shopsphere.config.AppConfig.get().freeShippingThreshold();
        if (taxableAmount.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return Money.scale(com.shopsphere.config.AppConfig.get().shippingFee());
    }

    private BigDecimal taxRate() {
        return com.shopsphere.config.AppConfig.get().taxRate();
    }

    public void addItem(long userId, long productId, int quantity) throws SQLException {
        if (quantity < 1) {
            quantity = 1;
        }
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            quantity = MAX_QUANTITY_PER_ITEM;
        }
        Optional<Product> product = productDao.findById(productId, false);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("The selected product is not available.");
        }
        if (product.get().getStock() < 1) {
            throw new IllegalStateException(product.get().getName() + " is out of stock.");
        }
        // The cart never holds more units than the catalogue can deliver.
        int alreadyInCart = cartDao.quantityOf(userId, productId);
        quantity = Math.min(quantity, Math.max(0, product.get().getStock() - alreadyInCart));
        if (quantity < 1) {
            throw new IllegalStateException("Only " + product.get().getStock()
                    + " left of " + product.get().getName() + ".");
        }
        cartDao.addItem(userId, productId, quantity);
    }

    /**
     * Sets the quantity of a cart line. The quantity is capped by the available stock.
     *
     * @return {@code true} when the line was updated
     */
    public boolean updateQuantity(long userId, long productId, int quantity) throws SQLException {
        if (quantity < 1) {
            removeItem(userId, productId);
            return true;
        }
        Optional<Product> product = productDao.findById(productId, false);
        if (product.isEmpty()) {
            removeItem(userId, productId);
            return false;
        }
        int capped = Math.min(quantity, MAX_QUANTITY_PER_ITEM);
        if (capped > product.get().getStock()) {
            capped = product.get().getStock();
        }
        if (capped < 1) {
            removeItem(userId, productId);
            return false;
        }
        cartDao.setQuantity(userId, productId, capped);
        return true;
    }

    public void removeItem(long userId, long productId) throws SQLException {
        cartDao.removeItem(userId, productId);
    }

    public void clear(long userId) throws SQLException {
        cartDao.clear(userId);
    }

    public int itemCount(long userId) throws SQLException {
        return cartDao.countItems(userId);
    }

    public boolean isCouponUsable(String code, BigDecimal subtotal, long userId) throws SQLException {
        if (code == null || code.isBlank()) {
            return false;
        }
        Optional<Coupon> coupon = couponDao.findByCode(code);
        return coupon.isPresent() && isUsable(coupon.get(), subtotal, userId);
    }

    /**
     * A coupon can be used when it is active, inside its date range, when the order
     * reaches the minimum amount, when the global usage limit is not reached and when
     * the customer has not used it more often than the per customer limit allows.
     */
    private boolean isUsable(Coupon coupon, BigDecimal subtotal, long userId) {
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
}
