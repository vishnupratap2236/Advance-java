package com.shopsphere.it;

import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.Product;
import com.shopsphere.service.AuthService;
import com.shopsphere.service.WishlistService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WishlistServiceIT {

    private final WishlistService wishlistService = new WishlistService();
    private final AuthService authService = new AuthService();
    private final ProductDao productDao = new ProductDao();

    private long userId;

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    private void createUser() {
        String email = TestSupport.uniqueEmail("wishlist");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Wishlist Customer", null);
        assertTrue(result.success());
        userId = result.user().getId();
    }

    @Test
    @DisplayName("a product can be saved to the wishlist and removed again")
    void togglesWishlistEntries() throws SQLException {
        createUser();
        try {
            Product product = productDao.findFeatured(50).stream()
                    .filter(p -> p.getStock() > 0)
                    .findFirst()
                    .orElseThrow();

            assertFalse(wishlistService.contains(userId, product.getId()));

            assertTrue(wishlistService.toggle(userId, product.getId()));
            assertTrue(wishlistService.contains(userId, product.getId()));

            List<Product> wishlist = wishlistService.list(userId);
            assertEquals(1, wishlist.size());
            assertEquals(product.getId(), wishlist.get(0).getId());

            assertFalse(wishlistService.toggle(userId, product.getId()));
            assertTrue(wishlistService.list(userId).isEmpty());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("the same product is never saved twice")
    void doesNotDuplicateEntries() throws Exception {
        createUser();
        try {
            Product product = productDao.findFeatured(50).stream()
                    .filter(p -> p.getStock() > 0)
                    .findFirst()
                    .orElseThrow();

            wishlistService.toggle(userId, product.getId());
            wishlistService.remove(userId, product.getId());
            wishlistService.toggle(userId, product.getId());

            assertEquals(1, wishlistService.list(userId).size());
            assertTrue(wishlistService.contains(userId, product.getId()));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}