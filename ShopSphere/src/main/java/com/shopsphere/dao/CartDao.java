package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.CartItem;
import com.shopsphere.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for shopping carts. A cart always belongs to exactly one user.
 */
public class CartDao {

    /** Returns the id of the cart of a user, creating it when it does not exist yet. */
    public long getOrCreateCartId(long userId) throws SQLException {
        try (Connection connection = Database.connection()) {
            return getOrCreateCartId(connection, userId);
        }
    }

    public long getOrCreateCartId(Connection connection, long userId) throws SQLException {
        String select = "SELECT id FROM carts WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        String insert = "INSERT INTO carts (user_id) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(insert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        // A concurrent request may have created the cart in the meantime.
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Could not create cart for user " + userId);
    }

    public List<CartItem> findItems(long userId) throws SQLException {
        String sql = """
                SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity,
                       p.id AS p_id, p.category_id, p.name, p.slug, p.sku, p.short_description,
                       p.description, p.price, p.compare_price, p.stock, p.image_url, p.active,
                       p.featured, p.rating, p.review_count,
                       c.name AS category_name, c.slug AS category_slug
                FROM cart_items ci
                JOIN carts c2 ON c2.id = ci.cart_id AND c2.user_id = ?
                JOIN products p ON p.id = ci.product_id
                JOIN categories c ON c.id = p.category_id
                ORDER BY ci.created_at, ci.id
                """;
        List<CartItem> items = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCartId(rs.getLong("cart_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));

                    Product product = new Product();
                    product.setId(rs.getLong("p_id"));
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
                    item.setProduct(product);
                    items.add(item);
                }
            }
        }
        return items;
    }

    /** Adds a product to the cart or increases the quantity of an existing line. */
    public void addItem(long userId, long productId, int quantity) throws SQLException {
        long cartId = getOrCreateCartId(userId);
        String update = "UPDATE cart_items SET quantity = quantity + ? "
                + "WHERE cart_id = ? AND product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartId);
            ps.setLong(3, productId);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String insert = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    public void setQuantity(long userId, long productId, int quantity) throws SQLException {
        String sql = "UPDATE cart_items ci JOIN carts c ON c.id = ci.cart_id "
                + "SET ci.quantity = ? WHERE c.user_id = ? AND ci.product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, userId);
            ps.setLong(3, productId);
            ps.executeUpdate();
        }
    }

    public void removeItem(long userId, long productId) throws SQLException {
        String sql = "DELETE ci FROM cart_items ci JOIN carts c ON c.id = ci.cart_id "
                + "WHERE c.user_id = ? AND ci.product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    public void clear(long userId) throws SQLException {
        String sql = "DELETE ci FROM cart_items ci JOIN carts c ON c.id = ci.cart_id "
                + "WHERE c.user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    /** The quantity of a product that is already in the cart of a user. */
    public int quantityOf(long userId, long productId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ci.quantity), 0) FROM cart_items ci "
                + "JOIN carts c ON c.id = ci.cart_id WHERE c.user_id = ? AND ci.product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Total number of units in the cart, used for the navigation badge. */
    public int countItems(long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ci.quantity), 0) FROM cart_items ci "
                + "JOIN carts c ON c.id = ci.cart_id WHERE c.user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
