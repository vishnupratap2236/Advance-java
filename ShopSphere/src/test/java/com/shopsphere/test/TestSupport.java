package com.shopsphere.test;

import com.shopsphere.db.Database;
import com.shopsphere.db.MigrationRunner;
import com.shopsphere.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Shared helpers for the database backed integration tests.
 */
public final class TestSupport {

    private TestSupport() {
    }

    /** A unique e-mail address for a test user. */
    public static String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@shopsphere.test";
    }

    /** A strong random password for a test user. */
    public static String randomPassword() {
        return PasswordHasher.randomPassword(14);
    }

    /** True when the configured database can be reached and the schema is up to date. */
    public static boolean databaseAvailable() {
        try {
            MigrationRunner.migrate();
            return Database.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    /** Removes a user and everything that belongs to it. */
    public static void deleteUser(long userId) {        try (Connection connection = Database.connection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM order_status_history WHERE order_id IN (SELECT id FROM orders WHERE user_id = ?)")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM orders WHERE user_id = ?")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = ?)")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM carts WHERE user_id = ?")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM addresses WHERE user_id = ?")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM login_attempts WHERE email = ?")) {
                    ps.setString(1, "");
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not clean up the test user", e);
        }
    }

    /** Removes every account that was created by a test. */
    public static void deleteAllTestUsers() {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT id FROM users WHERE email LIKE '%@shopsphere.test'");
             ResultSet rs = ps.executeQuery()) {
            java.util.List<Long> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
            for (Long id : ids) {
                deleteUser(id);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not clean up the test users", e);
        }
    }

    /** Reads a single scalar value from the database. */
    public static long scalar(String sql) throws SQLException {        try (Connection connection = Database.connection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    /** Reads a single string value from the database. */
    public static String text(String sql) throws SQLException {
        try (Connection connection = Database.connection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }
}
