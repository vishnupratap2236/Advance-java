package com.shopsphere.it;

import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.Review;
import com.shopsphere.service.AuthService;
import com.shopsphere.service.ReviewService;
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

class ReviewServiceIT {

    private final ReviewService reviewService = new ReviewService();
    private final AuthService authService = new AuthService();
    private final ProductDao productDao = new ProductDao();

    private long userId;
    private long productId;

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    private void createUser() {
        String email = TestSupport.uniqueEmail("review");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Review Customer", null);
        assertTrue(result.success());
        userId = result.user().getId();
    }

    @Test
    @DisplayName("a review is stored and the average rating of the product is refreshed")
    void addsReviewAndUpdatesRating() throws SQLException {
        createUser();
        productId = productDao.findFeatured(50).stream()
                .filter(p -> p.getStock() > 0)
                .findFirst()
                .orElseThrow()
                .getId();
        try {
            ReviewService.ReviewResult result = reviewService.add(productId, userId, 4,
                    "Solid product", "Works exactly as described.");

            assertTrue(result.success(), "Could not add the review: " + result.error());

            List<Review> reviews = reviewService.listForProduct(productId);
            assertTrue(reviews.stream().anyMatch(r -> r.getUserId() == userId));

            var product = productDao.findById(productId, true).get();
            assertEquals(1, product.getReviewCount());
            assertEquals(4.0, product.getRating().doubleValue(), 0.01);
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a customer can only keep one review per product and can update it")
    void updatesExistingReview() throws SQLException {
        createUser();
        productId = productDao.findFeatured(50).stream()
                .filter(p -> p.getStock() > 0)
                .findFirst()
                .orElseThrow()
                .getId();
        try {
            assertTrue(reviewService.add(productId, userId, 2, "First", "First review").success());
            assertTrue(reviewService.add(productId, userId, 5, "Second", "Second review").success());

            List<Review> reviews = reviewService.listForProduct(productId);
            assertEquals(1, reviews.stream().filter(r -> r.getUserId() == userId).count());
            assertEquals(5, reviews.get(0).getRating());
            assertEquals(1, productDao.findById(productId, true).get().getReviewCount());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("invalid reviews are rejected")
    void rejectsInvalidReviews() {
        createUser();
        try {
            assertFalse(reviewService.add(1L, userId, 0, "Title", "Comment").success());
            assertFalse(reviewService.add(1L, userId, 6, "Title", "Comment").success());
            assertFalse(reviewService.add(1L, userId, 3, null, null).success());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a customer can delete their own review but not the one of somebody else")
    void deletesOwnReviewOnly() throws SQLException {
        createUser();
        productId = productDao.findFeatured(50).stream()
                .filter(p -> p.getStock() > 0)
                .findFirst()
                .orElseThrow()
                .getId();
        try {
            reviewService.add(productId, userId, 3, "Title", "Comment");
            Review review = reviewService.findOwnReview(userId, productId).orElseThrow();

            assertFalse(reviewService.delete(review.getId(), userId + 1_000_000L));
            assertTrue(reviewService.delete(review.getId(), userId));
            assertTrue(reviewService.listForProduct(productId).stream()
                    .noneMatch(r -> r.getUserId() == userId));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}