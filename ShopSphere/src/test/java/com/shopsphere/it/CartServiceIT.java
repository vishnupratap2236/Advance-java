package com.shopsphere.it;

import com.shopsphere.model.CartItem;
import com.shopsphere.model.CartView;
import com.shopsphere.model.User;
import com.shopsphere.service.AuthService;
import com.shopsphere.service.CartService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CartServiceIT {

    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();

    private long userId;

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    private long createUser() {
        String email = TestSupport.uniqueEmail("cart");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Cart Customer", null);
        assertTrue(result.success());
        return result.user().getId();
    }

    @Test
    @DisplayName("a product can be added and the totals are calculated on the server")
    void addsItemsAndCalculatesTotals() throws SQLException {
        userId = createUser();
        try {
            var product = productInStock();

            cartService.addItem(userId, product.getId(), 2);

            CartView view = cartService.view(userId, null);

            assertEquals(1, view.getItems().size());
            assertEquals(2, view.getItemCount());
            assertEquals(product.getPrice().multiply(BigDecimal.valueOf(2)), view.getSubtotal());
            assertEquals(view.getSubtotal().add(view.getTax()).add(view.getShippingFee()), view.getTotal());
            assertEquals(2, cartService.itemCount(userId));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("adding the same product twice increases the quantity")
    void mergesDuplicateLines() throws SQLException {
        userId = createUser();
        try {
            var product = productInStock();

            cartService.addItem(userId, product.getId(), 1);
            cartService.addItem(userId, product.getId(), 2);

            CartView view = cartService.view(userId, null);

            assertEquals(1, view.getItems().size());
            assertEquals(3, view.getItems().get(0).getQuantity());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("the quantity is capped at the available stock")
    void capsQuantityAtStock() throws SQLException {
        userId = createUser();
        try {
            var product = productInStock();

            cartService.addItem(userId, product.getId(), 999);

            CartView view = cartService.view(userId, null);
            CartItem item = view.getItems().get(0);

            assertEquals(Math.min(product.getStock(), 20), item.getQuantity());
            assertTrue(item.getQuantity() <= product.getStock());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a product that is out of stock cannot be added")
    void rejectsOutOfStockProducts() throws SQLException {
        userId = createUser();
        try {
            var product = new com.shopsphere.dao.ProductDao()
                    .findLatest(50).stream()
                    .filter(p -> p.getStock() == 0)
                    .findFirst()
                    .orElse(null);
            if (product == null) {
                return; // no out of stock product in the demo catalogue
            }
            assertThrows(IllegalStateException.class,
                    () -> cartService.addItem(userId, product.getId(), 1));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("items can be removed and the cart can be cleared")
    void removesAndClears() throws SQLException {
        userId = createUser();
        try {
            var product = productInStock();
            cartService.addItem(userId, product.getId(), 1);

            cartService.removeItem(userId, product.getId());
            assertTrue(cartService.view(userId, null).isEmpty());

            cartService.addItem(userId, product.getId(), 1);
            cartService.clear(userId);
            assertTrue(cartService.view(userId, null).isEmpty());
            assertEquals(0, cartService.itemCount(userId));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a valid coupon lowers the total, an invalid one is reported")
    void appliesCoupons() throws SQLException {
        userId = createUser();
        try {
            var product = productInStock();
            cartService.addItem(userId, product.getId(), 2);

            CartView withCoupon = cartService.view(userId, "WELCOME10");
            assertTrue(withCoupon.getDiscount().compareTo(BigDecimal.ZERO) > 0);
            assertEquals("WELCOME10", withCoupon.getCouponCode());

            CartView withoutCoupon = cartService.view(userId, null);
            assertEquals(BigDecimal.ZERO, withoutCoupon.getDiscount());

            CartView badCoupon = cartService.view(userId, "NOPE");
            assertEquals(BigDecimal.ZERO, badCoupon.getDiscount());
            assertFalse(badCoupon.getCouponMessage() == null);
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    /** A product of the demo catalogue that is currently in stock. */
    private com.shopsphere.model.Product productInStock() throws SQLException {
        return new com.shopsphere.dao.ProductDao().findFeatured(50).stream()
                .filter(p -> p.getStock() > 5)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No product in stock for the test"));
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}