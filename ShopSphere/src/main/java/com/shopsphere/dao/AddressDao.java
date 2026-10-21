package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the delivery address book. Every read is scoped to the owner,
 * which prevents a customer from reading somebody else's addresses.
 */
public class AddressDao {

    public List<Address> findByUser(long userId) throws SQLException {
        String sql = "SELECT id, user_id, recipient_name, phone, line1, line2, city, state, "
                + "postal_code, country, is_default, created_at, updated_at FROM addresses "
                + "WHERE user_id = ? ORDER BY is_default DESC, id DESC";
        List<Address> addresses = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(map(rs));
                }
            }
        }
        return addresses;
    }

    public Optional<Address> findById(long id, long userId) throws SQLException {
        String sql = "SELECT id, user_id, recipient_name, phone, line1, line2, city, state, "
                + "postal_code, country, is_default, created_at, updated_at FROM addresses "
                + "WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Address> findDefault(long userId) throws SQLException {
        String sql = "SELECT id, user_id, recipient_name, phone, line1, line2, city, state, "
                + "postal_code, country, is_default, created_at, updated_at FROM addresses "
                + "WHERE user_id = ? ORDER BY is_default DESC, id DESC LIMIT 1";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public long insert(Address address) throws SQLException {
        String sql = "INSERT INTO addresses (user_id, recipient_name, phone, line1, line2, city, "
                + "state, postal_code, country, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setLong(i++, address.getUserId());
            ps.setString(i++, address.getRecipientName());
            ps.setString(i++, address.getPhone());
            ps.setString(i++, address.getLine1());
            ps.setString(i++, address.getLine2());
            ps.setString(i++, address.getCity());
            ps.setString(i++, address.getState());
            ps.setString(i++, address.getPostalCode());
            ps.setString(i++, address.getCountry());
            ps.setBoolean(i, address.isDefaultAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    address.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create address");
    }

    public boolean update(Address address) throws SQLException {
        String sql = "UPDATE addresses SET recipient_name = ?, phone = ?, line1 = ?, line2 = ?, "
                + "city = ?, state = ?, postal_code = ?, country = ?, is_default = ? "
                + "WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, address.getRecipientName());
            ps.setString(i++, address.getPhone());
            ps.setString(i++, address.getLine1());
            ps.setString(i++, address.getLine2());
            ps.setString(i++, address.getCity());
            ps.setString(i++, address.getState());
            ps.setString(i++, address.getPostalCode());
            ps.setString(i++, address.getCountry());
            ps.setBoolean(i++, address.isDefaultAddress());
            ps.setLong(i++, address.getId());
            ps.setLong(i, address.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void clearDefault(long userId) throws SQLException {
        String sql = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public long countForUser(long userId) throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM addresses WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static Address map(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setId(rs.getLong("id"));
        address.setUserId(rs.getLong("user_id"));
        address.setRecipientName(rs.getString("recipient_name"));
        address.setPhone(rs.getString("phone"));
        address.setLine1(rs.getString("line1"));
        address.setLine2(rs.getString("line2"));
        address.setCity(rs.getString("city"));
        address.setState(rs.getString("state"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountry(rs.getString("country"));
        address.setDefaultAddress(rs.getBoolean("is_default"));
        return address;
    }
}
