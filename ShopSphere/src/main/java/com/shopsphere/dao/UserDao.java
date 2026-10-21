package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Role;
import com.shopsphere.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for user accounts and login attempts.
 */
public class UserDao {

    private static final String SELECT_COLUMNS =
            "id, email, password_hash, full_name, phone, role, status, created_at, updated_at";

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE email = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email == null ? null : email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    public long insert(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, full_name, phone, role, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create user");
    }

    public boolean updateProfile(long userId, String fullName, String phone) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(long userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public long countByRole(Role role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public PageResult<User> findCustomers(int page, int pageSize) throws SQLException {
        long total = countCustomers();
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE role = 'CUSTOMER' "
                + "ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        }
        return new PageResult<>(users, page, pageSize, total);
    }

    public long countCustomers() throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    // ------------------------------------------------------ login attempts

    public void recordLoginAttempt(String email, String ipAddress, boolean success) throws SQLException {
        String sql = "INSERT INTO login_attempts (email, ip_address, success) VALUES (?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim().toLowerCase());
            ps.setString(2, ipAddress);
            ps.setBoolean(3, success);
            ps.executeUpdate();
        }
    }

    /** Number of failed attempts for an email inside the given window. */
    public int countRecentFailures(String email, LocalDateTime since) throws SQLException {
        String sql = "SELECT COUNT(*) FROM login_attempts "
                + "WHERE email = ? AND success = 0 AND attempted_at >= ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim().toLowerCase());
            ps.setTimestamp(2, Timestamp.valueOf(since));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void clearLoginAttempts(String email) throws SQLException {
        String sql = "DELETE FROM login_attempts WHERE email = ? AND success = 0";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim().toLowerCase());
            ps.executeUpdate();
        }
    }

    /** Removes old login attempt rows; called opportunistically to keep the table small. */
    public void purgeOldAttempts(LocalDateTime before) throws SQLException {
        String sql = "DELETE FROM login_attempts WHERE attempted_at < ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(before));
            ps.executeUpdate();
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            user.setUpdatedAt(updated.toLocalDateTime());
        }
        return user;
    }
}
