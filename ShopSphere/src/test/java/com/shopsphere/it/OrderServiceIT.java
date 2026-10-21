package com.shopsphere.it;

import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.CartView;
import com.shopsphere.model.Order;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.Product;
import com.shopsphere.service.AuthService;
import com.shopsphere.service.CartService;
import com.shopsphere.service.OrderService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OrderServiceIT {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();
    private final ProductDao productDao = new ProductDao();

    private long userId;

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    private void createUser() {
        String email = TestSupport.uniqueEmail("order");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Order Customer", null);
        assertTrue(result.success());
        userId = result.user().getId();
    }

    private OrderService.CheckoutRequest request(String coupon) {
        return new OrderService.CheckoutRequest(userId, "Test Recipient", "+91 90000 00000",
                "12 MG Road", "Near the park", "Bengaluru", "Karnataka", "560001", "India",
                "Leave at the reception", "COD", coupon);
    }

    @Test
    @DisplayName("placing an order stores it, lowers the stock and empties the cart")
    void placesOrder() throws Exception {
        createUser();
        try {
            Product product = productInStock();
            int stockBefore = product.getStock();

            cartService.addItem(userId, product.getId(), 2);
            CartView cart = cartService.view(userId, null);

            Order order = orderService.placeOrder(request(null));

            assertNotNull(order.getOrderNumber());
            assertTrue(order.getOrderNumber().startsWith("SS-"));
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(cart.getSubtotal(), order.getSubtotal());
            assertEquals(cart.getTotal(), order.getTotal());
            assertEquals(1, order.getItems().size());
            assertEquals(2, order.getItems().get(0).getQuantity());
            assertEquals(product.getPrice().multiply(BigDecimal.valueOf(2)),
                    order.getItems().get(0).getLineTotal());

            assertEquals(stockBefore - 2, productDao.findById(product.getId(), true).get().getStock());
            assertTrue(cartService.view(userId, null).isEmpty());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a customer can only see their own orders")
    void enforcesOrderOwnership() throws Exception {
        createUser();
        long otherUserId = userId;
        try {
            Product product = productInStock();
            cartService.addItem(userId, product.getId(), 1);
            Order order = orderService.placeOrder(request(null));

            assertTrue(orderService.findForUser(order.getId(), userId).isPresent());
            assertTrue(orderService.findForUser(order.getId(), otherUserId + 1_000_000L).isEmpty());

            var page = orderService.listForUser(userId, 1, 10);
            assertEquals(1, page.getTotalItems());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("an order cannot be placed when the stock is not sufficient")
    void refusesToOversell() throws Exception {
        createUser();
        Product product = productInStock();
        int originalStock = product.getStock();
        try {
            cartService.addItem(userId, product.getId(), 1);

            // Simulate a competing order that took the last unit.
            try (var connection = com.shopsphere.db.Database.connection()) {
                try (var ps = connection.prepareStatement(
                        "UPDATE products SET stock = 0 WHERE id = ?")) {
                    ps.setLong(1, product.getId());
                    ps.executeUpdate();
                }
            }

            int stockBefore = productDao.findById(product.getId(), true).get().getStock();

            assertThrows(OrderService.CheckoutException.class,
                    () -> orderService.placeOrder(request(null)));

            // The stock must not have gone negative and no order may exist.
            assertEquals(stockBefore, productDao.findById(product.getId(), true).get().getStock());
            assertEquals(0, orderService.listForUser(userId, 1, 10).getTotalItems());
        } finally {
            // Restore the demo catalogue so that the test leaves no trace.
            try (var connection = com.shopsphere.db.Database.connection();
                 var ps = connection.prepareStatement("UPDATE products SET stock = ? WHERE id = ?")) {
                ps.setInt(1, originalStock);
                ps.setLong(2, product.getId());
                ps.executeUpdate();
            }
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("cancelling an order returns the stock to the catalogue")
    void cancellingRestoresStock() throws Exception {
        createUser();
        try {
            Product product = productInStock();
            int stockBefore = product.getStock();

            cartService.addItem(userId, product.getId(), 2);
            Order order = orderService.placeOrder(request(null));
            assertEquals(stockBefore - 2, productDao.findById(product.getId(), true).get().getStock());

            assertTrue(orderService.cancelByCustomer(order.getId(), userId));
            assertEquals(stockBefore, productDao.findById(product.getId(), true).get().getStock());

            Optional<Order> reloaded = orderService.findById(order.getId());
            assertTrue(reloaded.isPresent());
            assertEquals(OrderStatus.CANCELLED, reloaded.get().getStatus());
            assertFalse(reloaded.get().getHistory().isEmpty());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a customer cannot cancel an order of somebody else")
    void doesNotCancelForeignOrders() throws Exception {
        createUser();
        try {
            Product product = productInStock();
            cartService.addItem(userId, product.getId(), 1);
            Order order = orderService.placeOrder(request(null));

            assertFalse(orderService.cancelByCustomer(order.getId(), userId + 1_000_000L));
            assertEquals(OrderStatus.PENDING, orderService.findById(order.getId()).get().getStatus());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("an administrator can change the status of an order")
    void adminUpdatesStatus() throws Exception {
        createUser();
        try {
            Product product = productInStock();
            cartService.addItem(userId, product.getId(), 1);
            Order order = orderService.placeOrder(request(null));

            assertTrue(orderService.updateStatus(order.getId(), OrderStatus.SHIPPED,
                    "Handed to the courier", null));

            Order updated = orderService.findById(order.getId()).get();
            assertEquals(OrderStatus.SHIPPED, updated.getStatus());
            assertEquals(updated.getHistory().size(), 2);
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a coupon is applied to the order and its usage is counted")
    void appliesCouponToOrder() throws Exception {
        createUser();
        try {
            Product product = productInStock();
            cartService.addItem(userId, product.getId(), 2);
            CartView cart = cartService.view(userId, "WELCOME10");

            Order order = orderService.placeOrder(request("WELCOME10"));

            assertTrue(order.getDiscount().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(cart.getDiscount(), order.getDiscount());
            assertEquals("WELCOME10", order.getCouponCode());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("an empty cart cannot be checked out")
    void refusesEmptyCart() throws Exception {
        createUser();
        try {
            assertThrows(OrderService.CheckoutException.class,
                    () -> orderService.placeOrder(request(null)));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    /** A product of the demo catalogue that is currently in stock. */
    private Product productInStock() throws SQLException {
        return productDao.findFeatured(50).stream()
                .filter(p -> p.getStock() > 5)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No product in stock for the test"));
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}