package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductImage;
import com.shopsphere.model.ProductQuery;

import java.math.BigDecimal;
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
 * Data access for products and their images.
 */
public class ProductDao {

    private static final String SELECT_BASE = """
            SELECT p.id, p.category_id, p.name, p.slug, p.sku, p.short_description, p.description,
                   p.price, p.compare_price, p.stock, p.image_url, p.active, p.featured,
                   p.rating, p.review_count, p.created_at, p.updated_at,
                   c.name AS category_name, c.slug AS category_slug
            FROM products p
            JOIN categories c ON c.id = p.category_id
            """;

    public Optional<Product> findById(long id, boolean includeInactive) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ?" + (includeInactive ? "" : " AND p.active = 1");
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Product product = map(rs);
                product.setImages(findImages(connection, product.getId()));
                return Optional.of(product);
            }
        }
    }

    public Optional<Product> findBySlug(String slug) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.slug = ? AND p.active = 1";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Product product = map(rs);
                product.setImages(findImages(connection, product.getId()));
                return Optional.of(product);
            }
        }
    }

    public Optional<Product> findBySku(String sku) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.sku = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<Product> search(ProductQuery query) throws SQLException {
        long total = count(query);
        List<Product> products = new ArrayList<>();
        String sql = SELECT_BASE + buildWhere(query) + " ORDER BY " + query.getSort().getOrderBy()
                + " LIMIT ? OFFSET ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = bindFilters(ps, query, 1);
            ps.setInt(index++, query.getPageSize());
            ps.setInt(index, (query.getPage() - 1) * query.getPageSize());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return new PageResult<>(products, query.getPage(), query.getPageSize(), total);
    }

    public long count(ProductQuery query) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products p JOIN categories c ON c.id = p.category_id "
                + buildWhere(query);
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindFilters(ps, query, 1);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public List<Product> findFeatured(int limit) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.active = 1 AND p.featured = 1 "
                + "ORDER BY p.rating DESC, p.id DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public List<Product> findLatest(int limit) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.active = 1 ORDER BY p.created_at DESC, p.id DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public List<Product> findByCategory(long categoryId, int limit) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.active = 1 AND p.category_id = ? "
                + "ORDER BY p.featured DESC, p.rating DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public List<Product> findRelated(Product product, int limit) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.active = 1 AND p.category_id = ? AND p.id <> ? "
                + "ORDER BY p.featured DESC, p.rating DESC LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, product.getCategoryId());
            ps.setLong(2, product.getId());
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    /** Products whose stock dropped to or below the given threshold. */
    public List<Product> findLowStock(int threshold, int limit) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.active = 1 AND p.stock <= ? ORDER BY p.stock ASC LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public long countAll() throws SQLException {
        return countScalar("SELECT COUNT(*) FROM products");
    }

    public long countActive() throws SQLException {
        return countScalar("SELECT COUNT(*) FROM products WHERE active = 1");
    }

    public long countLowStock(int threshold) throws SQLException {
        return countScalar("SELECT COUNT(*) FROM products WHERE active = 1 AND stock <= " + threshold);
    }

    public long countOutOfStock() throws SQLException {
        return countScalar("SELECT COUNT(*) FROM products WHERE active = 1 AND stock = 0");
    }

    private long countScalar(String sql) throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public long insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (category_id, name, slug, sku, short_description, description, "
                + "price, compare_price, stock, image_url, active, featured, rating, review_count) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setLong(i++, product.getCategoryId());
            ps.setString(i++, product.getName());
            ps.setString(i++, product.getSlug());
            ps.setString(i++, product.getSku());
            ps.setString(i++, product.getShortDescription());
            ps.setString(i++, product.getDescription());
            ps.setBigDecimal(i++, product.getPrice());
            ps.setBigDecimal(i++, product.getComparePrice());
            ps.setInt(i++, product.getStock());
            ps.setString(i++, product.getImageUrl());
            ps.setBoolean(i++, product.isActive());
            ps.setBoolean(i++, product.isFeatured());
            ps.setBigDecimal(i++, product.getRating());
            ps.setInt(i, product.getReviewCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    product.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create product");
    }

    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, slug = ?, sku = ?, "
                + "short_description = ?, description = ?, price = ?, compare_price = ?, stock = ?, "
                + "image_url = ?, active = ?, featured = ?, rating = ?, review_count = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            ps.setLong(i++, product.getCategoryId());
            ps.setString(i++, product.getName());
            ps.setString(i++, product.getSlug());
            ps.setString(i++, product.getSku());
            ps.setString(i++, product.getShortDescription());
            ps.setString(i++, product.getDescription());
            ps.setBigDecimal(i++, product.getPrice());
            ps.setBigDecimal(i++, product.getComparePrice());
            ps.setInt(i++, product.getStock());
            ps.setString(i++, product.getImageUrl());
            ps.setBoolean(i++, product.isActive());
            ps.setBoolean(i++, product.isFeatured());
            ps.setBigDecimal(i++, product.getRating());
            ps.setInt(i++, product.getReviewCount());
            ps.setLong(i, product.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Sets the stock of a product. Returns the number of affected rows.
     */
    public boolean setStock(long id, int stock) throws SQLException {
        String sql = "UPDATE products SET stock = ? WHERE id = ? AND ? >= 0";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, stock);
            ps.setLong(2, id);
            ps.setInt(3, stock);
            return ps.executeUpdate() > 0;
        }
    }

    public void addImage(Connection connection, long productId, String imageUrl, String altText,
                         int sortOrder) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, alt_text, sort_order) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, imageUrl);
            ps.setString(3, altText);
            ps.setInt(4, sortOrder);
            ps.executeUpdate();
        }
    }

    public void deleteImages(long productId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE product_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        }
    }

    public List<ProductImage> findImages(Connection connection, long productId) throws SQLException {
        String sql = "SELECT id, product_id, image_url, alt_text, sort_order FROM product_images "
                + "WHERE product_id = ? ORDER BY sort_order, id";
        List<ProductImage> images = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductImage image = new ProductImage();
                    image.setId(rs.getLong("id"));
                    image.setProductId(rs.getLong("product_id"));
                    image.setImageUrl(rs.getString("image_url"));
                    image.setAltText(rs.getString("alt_text"));
                    image.setSortOrder(rs.getInt("sort_order"));
                    images.add(image);
                }
            }
        }
        return images;
    }

    // ------------------------------------------------------------- filters

    private String buildWhere(ProductQuery query) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1 ");
        if (!query.isIncludeInactive()) {
            where.append("AND p.active = 1 ");
        }
        if (query.getSearch() != null && !query.getSearch().isBlank()) {
            where.append("AND (p.name LIKE ? OR p.short_description LIKE ? OR p.description LIKE ? "
                    + "OR p.sku LIKE ?) ");
        }
        if (query.getCategoryId() != null) {
            where.append("AND p.category_id = ? ");
        }
        if (query.getMinPrice() != null) {
            where.append("AND p.price >= ? ");
        }
        if (query.getMaxPrice() != null) {
            where.append("AND p.price <= ? ");
        }
        if (query.isOnlyInStock()) {
            where.append("AND p.stock > 0 ");
        }
        if (query.isOnlyFeatured()) {
            where.append("AND p.featured = 1 ");
        }
        return where.toString();
    }

    private int bindFilters(PreparedStatement ps, ProductQuery query, int index) throws SQLException {
        if (query.getSearch() != null && !query.getSearch().isBlank()) {
            String pattern = "%" + query.getSearch().trim() + "%";
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
            ps.setString(index++, pattern);
        }
        if (query.getCategoryId() != null) {
            ps.setLong(index++, query.getCategoryId());
        }
        if (query.getMinPrice() != null) {
            ps.setBigDecimal(index++, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            ps.setBigDecimal(index++, query.getMaxPrice());
        }
        return index;
    }

    private static Product map(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setCategoryId(rs.getLong("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setCategorySlug(rs.getString("category_slug"));
        product.setName(rs.getString("name"));
        product.setSlug(rs.getString("slug"));
        product.setSku(rs.getString("sku"));
        product.setShortDescription(rs.getString("short_description"));
        product.setDescription(rs.getString("description"));
        BigDecimal price = rs.getBigDecimal("price");
        product.setPrice(price == null ? BigDecimal.ZERO : price);
        BigDecimal comparePrice = rs.getBigDecimal("compare_price");
        product.setComparePrice(comparePrice);
        product.setStock(rs.getInt("stock"));
        product.setImageUrl(rs.getString("image_url"));
        product.setActive(rs.getBoolean("active"));
        product.setFeatured(rs.getBoolean("featured"));
        BigDecimal rating = rs.getBigDecimal("rating");
        product.setRating(rating == null ? BigDecimal.ZERO : rating);
        product.setReviewCount(rs.getInt("review_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            product.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            product.setUpdatedAt(updated.toLocalDateTime());
        }
        return product;
    }
}
