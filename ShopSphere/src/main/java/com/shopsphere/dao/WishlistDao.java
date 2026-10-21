package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the wishlist of a customer.
 */
public class WishlistDao {

    public void add(long userId, long productId) throws SQLException {
        String sql = "INSERT INTO wishlist_items (user_id, product_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    public void remove(long userId, long productId) throws SQLException {
        String sql = "DELETE FROM wishlist_items WHERE user_id = ? AND product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    public boolean contains(long userId, long productId) throws SQLException {
        String sql = "SELECT 1 FROM wishlist_items WHERE user_id = ? AND product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Product> findProducts(long userId) throws SQLException {
        String sql = """
                SELECT p.id, p.category_id, p.name, p.slug, p.sku, p.short_description, p.description,
                       p.price, p.compare_price, p.stock, p.image_url, p.active, p.featured,
                       p.rating, p.review_count, p.created_at, p.updated_at,
                       c.name AS category_name, c.slug AS category_slug
                FROM wishlist_items w
                JOIN products p ON p.id = w.product_id
                JOIN categories c ON c.id = p.category_id
                WHERE w.user_id = ? AND p.active = 1
                ORDER BY w.created_at DESC
                """;
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getLong("id"));
                    product.setCategoryId(rs.getLong("category_id"));
                    product.setName(rs.getString("name"));
                    product.setSlug(rs.getString("slug"));
                    product.setSku(rs.getString("sku"));
                    product.setShortDescription(rs.getString("short_description"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setComparePrice(rs.getBigDecimal("compare_price"));
                    product.setStock(rs.getInt("stock"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setActive(rs.getBoolean("active"));
                    product.setFeatured(rs.getBoolean("featured"));
                    product.setRating(rs.getBigDecimal("rating"));
                    product.setReviewCount(rs.getInt("review_count"));
                    product.setCategoryName(rs.getString("category_name"));
                    product.setCategorySlug(rs.getString("category_slug"));
                    products.add(product);
                }
            }
        }
        return products;
    }
}
