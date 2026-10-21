package com.shopsphere.service;

import com.shopsphere.dao.ReviewDao;
import com.shopsphere.db.Database;
import com.shopsphere.db.Transactions;
import com.shopsphere.model.Review;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Product reviews written by customers.
 */
public class ReviewService {

    private static final Logger LOG = Logger.getLogger(ReviewService.class.getName());

    private final ReviewDao reviewDao = new ReviewDao();

    public List<Review> listForProduct(long productId) throws SQLException {
        return reviewDao.findByProduct(productId);
    }

    public Optional<Review> findOwnReview(long userId, long productId) throws SQLException {
        return reviewDao.findByUserAndProduct(userId, productId);
    }

    /** Result of a review submission. */
    public record ReviewResult(boolean success, String error) {
    }

    /**
     * Stores a review and refreshes the average rating of the product.
     */
    public ReviewResult add(long productId, long userId, int rating, String title, String comment) {
        if (rating < 1 || rating > 5) {
            return new ReviewResult(false, "Choose a rating between 1 and 5 stars.");
        }
        if (title != null && title.length() > 150) {
            return new ReviewResult(false, "The review title is too long.");
        }
        if (comment != null && comment.length() > 2000) {
            return new ReviewResult(false, "The review is too long.");
        }
        if ((title == null || title.isBlank()) && (comment == null || comment.isBlank())) {
            return new ReviewResult(false, "Write a short review before submitting it.");
        }
        try {
            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(userId);
            review.setRating(rating);
            review.setTitle(title == null || title.isBlank() ? null : title.trim());
            review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
            review.setStatus("PUBLISHED");

            Transactions.inTransaction(connection -> {
                Optional<Review> existing = reviewDao.findByUserAndProduct(userId, productId);
                if (existing.isPresent()) {
                    review.setId(existing.get().getId());
                    try (var ps = connection.prepareStatement(
                            "UPDATE reviews SET rating = ?, title = ?, comment = ?, status = 'PUBLISHED' "
                                    + "WHERE id = ?")) {
                        ps.setInt(1, review.getRating());
                        ps.setString(2, review.getTitle());
                        ps.setString(3, review.getComment());
                        ps.setLong(4, review.getId());
                        ps.executeUpdate();
                    }
                } else {
                    reviewDao.insert(review);
                }
                reviewDao.refreshProductRating(connection, productId);
                return null;
            });
            return new ReviewResult(true, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not store the review", e);
            return new ReviewResult(false, "The review could not be saved. Please try again.");
        }
    }

    public boolean delete(long reviewId, long userId) throws SQLException {
        Optional<Review> review = reviewDao.findById(reviewId);
        if (review.isEmpty() || review.get().getUserId() != userId) {
            return false;
        }
        long productId = review.get().getProductId();
        boolean deleted = reviewDao.delete(reviewId, userId);
        if (deleted) {
            Transactions.inTransaction(connection -> {
                reviewDao.refreshProductRating(connection, productId);
                return null;
            });
        }
        return deleted;
    }
}
