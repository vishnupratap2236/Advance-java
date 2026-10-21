package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Coupon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for discount coupons.
 */
public class CouponDao {

    public Optional<Coupon> findByCode(String code) throws SQLException {
        String sql = "SELECT id, code, discount_percent, min_order_amount, max_discount, usage_limit, "
                + "times_used, starts_at, expires_at, active FROM coupons WHERE code = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code == null ? null : code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Coupon> findAll() throws SQLException {
        String sql = "SELECT id, code, discount_percent, min_order_amount, max_discount, usage_limit, "
                + "times_used, starts_at, expires_at, active FROM coupons ORDER BY code";
        List<Coupon> coupons = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                coupons.add(map(rs));
            }
        }
        return coupons;
    }

    public long insert(Coupon coupon) throws SQLException {
        String sql = "INSERT INTO coupons (code, discount_percent, min_order_amount, max_discount, "
                + "usage_limit, starts_at, expires_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, coupon.getCode().toUpperCase());
            ps.setBigDecimal(2, coupon.getDiscountPercent());
            ps.setBigDecimal(3, coupon.getMinOrderAmount());
            ps.setBigDecimal(4, coupon.getMaxDiscount());
            if (coupon.getUsageLimit() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, coupon.getUsageLimit());
            }
            ps.setTimestamp(6, Timestamp.valueOf(coupon.getStartsAt() == null
                    ? LocalDateTime.now() : coupon.getStartsAt()));
            if (coupon.getExpiresAt() == null) {
                ps.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(coupon.getExpiresAt()));
            }
            ps.setBoolean(8, coupon.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    coupon.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create coupon");
    }

    public boolean update(Coupon coupon) throws SQLException {
        String sql = "UPDATE coupons SET code = ?, discount_percent = ?, min_order_amount = ?, "
                + "max_discount = ?, usage_limit = ?, expires_at = ?, active = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, coupon.getCode().toUpperCase());
            ps.setBigDecimal(2, coupon.getDiscountPercent());
            ps.setBigDecimal(3, coupon.getMinOrderAmount());
            ps.setBigDecimal(4, coupon.getMaxDiscount());
            if (coupon.getUsageLimit() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, coupon.getUsageLimit());
            }
            if (coupon.getExpiresAt() == null) {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, Timestamp.valueOf(coupon.getExpiresAt()));
            }
            ps.setBoolean(7, coupon.isActive());
            ps.setLong(8, coupon.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM coupons WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Increments the usage counter of a coupon inside the checkout transaction. */
    public void incrementUsage(Connection connection, long couponId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE coupons SET times_used = times_used + 1 WHERE id = ?")) {
            ps.setLong(1, couponId);
            ps.executeUpdate();
        }
    }

    /** Records that a customer used a coupon, so that the per user limit can be enforced. */
    public void recordUsage(Connection connection, long couponId, long userId, Long orderId,
                             java.math.BigDecimal discountApplied) throws SQLException {
        String sql = "INSERT INTO coupon_usage (coupon_id, user_id, order_id, discount_applied) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, couponId);
            ps.setLong(2, userId);
            if (orderId == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, orderId);
            }
            ps.setBigDecimal(4, discountApplied == null ? java.math.BigDecimal.ZERO : discountApplied);
            ps.executeUpdate();
        }
    }

    /** Number of times a customer already used a coupon. */
    public int countUsageByUser(long couponId, long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM coupon_usage WHERE coupon_id = ? AND user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, couponId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static Coupon map(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getLong("id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        coupon.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        coupon.setMaxDiscount(rs.getBigDecimal("max_discount"));
        int usageLimit = rs.getInt("usage_limit");
        coupon.setUsageLimit(rs.wasNull() ? null : usageLimit);
        coupon.setTimesUsed(rs.getInt("times_used"));
        Timestamp starts = rs.getTimestamp("starts_at");
        if (starts != null) {
            coupon.setStartsAt(starts.toLocalDateTime());
        }
        Timestamp expires = rs.getTimestamp("expires_at");
        if (expires != null) {
            coupon.setExpiresAt(expires.toLocalDateTime());
        }
        coupon.setActive(rs.getBoolean("active"));
        return coupon;
    }
}
