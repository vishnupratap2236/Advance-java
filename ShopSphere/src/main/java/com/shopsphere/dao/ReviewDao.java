package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the product reviews.
 */
public class ReviewDao {

    public long insert(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, title, comment, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getTitle());
            ps.setString(5, review.getComment());
            ps.setString(6, review.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    review.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create the review");
    }

    public List<Review> findByProduct(long productId) throws SQLException {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.rating, r.title, r.comment, r.status, "
                + "r.created_at, u.full_name AS author_name FROM reviews r "
                + "JOIN users u ON u.id = r.user_id "
                + "WHERE r.product_id = ? AND r.status = 'PUBLISHED' ORDER BY r.created_at DESC, r.id DESC";
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(map(rs));
                }
            }
        }
        return reviews;
    }

    public Optional<Review> findByUserAndProduct(long userId, long productId) throws SQLException {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.rating, r.title, r.comment, r.status, "
                + "r.created_at, u.full_name AS author_name FROM reviews r "
                + "JOIN users u ON u.id = r.user_id WHERE r.user_id = ? AND r.product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Review> findById(long id) throws SQLException {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.rating, r.title, r.comment, r.status, "
                + "r.created_at, u.full_name AS author_name FROM reviews r "
                + "JOIN users u ON u.id = r.user_id WHERE r.id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Recalculates the average rating and the review count of a product. */
    public void refreshProductRating(Connection connection, long productId) throws SQLException {
        String sql = "UPDATE products p SET "
                + "p.rating = COALESCE((SELECT ROUND(AVG(r.rating), 2) FROM reviews r "
                + "                WHERE r.product_id = p.id AND r.status = 'PUBLISHED'), 0), "
                + "p.review_count = (SELECT COUNT(*) FROM reviews r "
                + "                  WHERE r.product_id = p.id AND r.status = 'PUBLISHED') "
                + "WHERE p.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        }
    }

    private static Review map(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setProductId(rs.getLong("product_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setAuthorName(rs.getString("author_name"));
        review.setRating(rs.getInt("rating"));
        review.setTitle(rs.getString("title"));
        review.setComment(rs.getString("comment"));
        review.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            review.setCreatedAt(created.toLocalDateTime());
        }
        return review;
    }
}
