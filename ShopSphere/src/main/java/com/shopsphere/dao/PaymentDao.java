package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Payment;

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
 * Data access for the payment records of an order.
 */
public class PaymentDao {

    public long insert(Connection connection, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, payment_method, transaction_id, amount, "
                + "payment_status, payment_gateway_response, paid_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getOrderId());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getTransactionId());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getPaymentStatus());
            ps.setString(6, payment.getGatewayResponse());
            if (payment.getPaidAt() == null) {
                ps.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(payment.getPaidAt()));
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    payment.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Could not create the payment record");
    }

    public Optional<Payment> findByOrderId(long orderId) throws SQLException {
        String sql = "SELECT id, order_id, payment_method, transaction_id, amount, payment_status, "
                + "payment_gateway_response, paid_at, created_at FROM payments "
                + "WHERE order_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT id, order_id, payment_method, transaction_id, amount, payment_status, "
                + "payment_gateway_response, paid_at, created_at FROM payments ORDER BY id DESC";
        List<Payment> payments = new ArrayList<>();
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(map(rs));
            }
        }
        return payments;
    }

    public boolean updateStatus(long id, String status, String transactionId, LocalDateTime paidAt)
            throws SQLException {
        String sql = "UPDATE payments SET payment_status = ?, transaction_id = ?, paid_at = ? WHERE id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, transactionId);
            if (paidAt == null) {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(paidAt));
            }
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Payment map(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setOrderId(rs.getLong("order_id"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        payment.setGatewayResponse(rs.getString("payment_gateway_response"));
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) {
            payment.setPaidAt(paid.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            payment.setCreatedAt(created.toLocalDateTime());
        }
        return payment;
    }
}
