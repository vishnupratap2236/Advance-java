package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Category;

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
 * Data access for product categories.
 */
public class CategoryDao {

    public List<Category> findAll(boolean onlyActive) throws SQLException {
        String sql = "SELECT c.id, c.name, c.slug, c.description, c.image_url, c.active, "
                + "c.created_at, c.updated_at, "
                + "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.id AND p.active = 1) AS product_count "
                + "FROM categories c "
                + (onlyActive ? "WHERE c.active = 1 " : "")
                + "ORDER BY c.name";
        List<Category> categories = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(map(rs));
            }
        }
        return categories;
    }

    public Optional<Category> findById(long id) throws SQLException {
        String sql = "SELECT c.id, c.name, c.slug, c.description, c.image_url, c.active, "
                + "c.created_at, c.updated_at, "
                + "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.id) AS product_count "
                + "FROM categories c WHERE c.id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Category> findBySlug(String slug) throws SQLException {
        String sql = "SELECT c.id, c.name, c.slug, c.description, c.image_url, c.active, "
                + "c.created_at, c.updated_at, "
                + "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.id AND p.active = 1) AS product_count "
                + "FROM categories c WHERE c.slug = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public long insert(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, slug, description, image_url, active) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getSlug());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getImageUrl());
            ps.setBoolean(5, category.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    category.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create category");
    }

    public boolean update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, slug = ?, description = ?, image_url = ?, "
                + "active = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getSlug());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getImageUrl());
            ps.setBoolean(5, category.isActive());
            ps.setLong(6, category.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Deletes a category that no longer holds products. */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ? AND NOT EXISTS "
                + "(SELECT 1 FROM products WHERE category_id = ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public long countProducts(long categoryId) throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM products WHERE category_id = ?")) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static Category map(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setSlug(rs.getString("slug"));
        category.setDescription(rs.getString("description"));
        category.setImageUrl(rs.getString("image_url"));
        category.setActive(rs.getBoolean("active"));
        category.setProductCount(rs.getInt("product_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            category.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            category.setUpdatedAt(updated.toLocalDateTime());
        }
        return category;
    }
}
