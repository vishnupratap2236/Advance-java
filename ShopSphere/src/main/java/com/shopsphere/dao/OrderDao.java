package com.shopsphere.dao;

import com.shopsphere.db.Database;
import com.shopsphere.model.Order;
import com.shopsphere.model.OrderItem;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.OrderStatusEvent;
import com.shopsphere.model.PageResult;

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
 * Data access for orders, order lines and the order status timeline.
 */
public class OrderDao {

    private static final String SELECT_BASE = """
            SELECT o.id, o.order_number, o.user_id, o.status, o.subtotal, o.discount, o.shipping_fee,
                   o.tax, o.total, o.coupon_code, o.payment_method, o.payment_status,
                   o.recipient_name, o.phone, o.address_line1, o.address_line2, o.city, o.state,
                   o.postal_code, o.country, o.notes, o.placed_at, o.updated_at,
                   u.full_name AS customer_name, u.email AS customer_email
            FROM orders o
            JOIN users u ON u.id = o.user_id
            """;

    /**
     * Inserts an order together with its lines. Must be called inside a transaction.
     */
    public long insert(Connection connection, Order order) throws SQLException {
        String sql = """
                INSERT INTO orders (order_number, user_id, status, subtotal, discount, shipping_fee,
                                    tax, total, coupon_code, payment_method, payment_status,
                                    recipient_name, phone, address_line1, address_line2, city, state,
                                    postal_code, country, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        long orderId;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setString(i++, order.getOrderNumber());
            ps.setLong(i++, order.getUserId());
            ps.setString(i++, order.getStatus().name());
            ps.setBigDecimal(i++, order.getSubtotal());
            ps.setBigDecimal(i++, order.getDiscount());
            ps.setBigDecimal(i++, order.getShippingFee());
            ps.setBigDecimal(i++, order.getTax());
            ps.setBigDecimal(i++, order.getTotal());
            ps.setString(i++, order.getCouponCode());
            ps.setString(i++, order.getPaymentMethod());
            ps.setString(i++, order.getPaymentStatus());
            ps.setString(i++, order.getRecipientName());
            ps.setString(i++, order.getPhone());
            ps.setString(i++, order.getAddressLine1());
            ps.setString(i++, order.getAddressLine2());
            ps.setString(i++, order.getCity());
            ps.setString(i++, order.getState());
            ps.setString(i++, order.getPostalCode());
            ps.setString(i++, order.getCountry());
            ps.setString(i, order.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not create order");
                }
                orderId = keys.getLong(1);
            }
        }
        order.setId(orderId);

        String itemSql = """
                INSERT INTO order_items (order_id, product_id, product_name, product_sku, image_url,
                                         unit_price, quantity, line_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(itemSql)) {
            for (OrderItem item : order.getItems()) {
                int i = 1;
                ps.setLong(i++, orderId);
                if (item.getProductId() == null) {
                    ps.setNull(i++, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(i++, item.getProductId());
                }
                ps.setString(i++, item.getProductName());
                ps.setString(i++, item.getProductSku());
                ps.setString(i++, item.getImageUrl());
                ps.setBigDecimal(i++, item.getUnitPrice());
                ps.setInt(i++, item.getQuantity());
                ps.setBigDecimal(i, item.getLineTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return orderId;
    }

    public void addStatusHistory(Connection connection, long orderId, OrderStatus status,
                                  String note, Long changedByUserId) throws SQLException {
        String sql = "INSERT INTO order_status_history (order_id, status, note, changed_by) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, status.name());
            ps.setString(3, note);
            if (changedByUserId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, changedByUserId);
            }
            ps.executeUpdate();
        }
    }

    public Optional<Order> findById(long id) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.id = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Order order = map(rs);
                order.setItems(findItems(connection, order.getId()));
                order.setHistory(findHistory(connection, order.getId()));
                return Optional.of(order);
            }
        }
    }

    public Optional<Order> findByOrderNumber(String orderNumber) throws SQLException {
        String sql = SELECT_BASE + "WHERE o.order_number = ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Order order = map(rs);
                order.setItems(findItems(connection, order.getId()));
                order.setHistory(findHistory(connection, order.getId()));
                return Optional.of(order);
            }
        }
    }

    /** All orders of a customer, newest first. */
    public PageResult<Order> findByUser(long userId, int page, int pageSize) throws SQLException {
        long total = countByUser(userId);
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE o.user_id = ? ORDER BY o.placed_at DESC, o.id DESC "
                + "LIMIT ? OFFSET ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
            for (Order order : orders) {
                order.setItems(findItems(connection, order.getId()));
            }
        }
        return new PageResult<>(orders, page, pageSize, total);
    }

    public PageResult<Order> findAll(int page, int pageSize, String status, String search)
            throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            where.append("AND o.status = ? ");
            params.add(OrderStatus.fromString(status).name());
        }
        if (search != null && !search.isBlank()) {
            where.append("AND (o.order_number LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?) ");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        long total = countAll(where.toString(), params);
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + where + "ORDER BY o.placed_at DESC, o.id DESC LIMIT ? OFFSET ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            ps.setInt(index++, pageSize);
            ps.setInt(index, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
            for (Order order : orders) {
                order.setItems(findItems(connection, order.getId()));
            }
        }
        return new PageResult<>(orders, page, pageSize, total);
    }

    public long countByUser(long userId) throws SQLException {
        return countScalar("SELECT COUNT(*) FROM orders WHERE user_id = ?", userId);
    }

    public long countAll() throws SQLException {
        return countScalar("SELECT COUNT(*) FROM orders", null);
    }

    public long countByStatus(OrderStatus status) throws SQLException {
        return countScalar("SELECT COUNT(*) FROM orders WHERE status = ?", status.name());
    }

    /** Revenue of all orders that were not cancelled. */
    public BigDecimal revenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM orders WHERE status <> 'CANCELLED'";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public List<Order> recentOrders(int limit) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY o.placed_at DESC, o.id DESC LIMIT ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
            for (Order order : orders) {
                order.setItems(findItems(connection, order.getId()));
            }
        }
        return orders;
    }

    public List<Order> topOrders(int limit) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE o.status <> 'CANCELLED' ORDER BY o.total DESC LIMIT ?";
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Updates the status of an order and records the change in the timeline.
     * Must be called inside a transaction.
     */
    public void updateStatus(Connection connection, long orderId, OrderStatus status, String note,
                             Long changedByUserId) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            ps.executeUpdate();
        }
        if (status == OrderStatus.DELIVERED) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE orders SET payment_status = 'PAID' WHERE id = ?")) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }
        }
        addStatusHistory(connection, orderId, status, note, changedByUserId);
    }

    public List<OrderItem> findItems(Connection connection, long orderId) throws SQLException {
        String sql = "SELECT id, order_id, product_id, product_name, product_sku, image_url, "
                + "unit_price, quantity, line_total FROM order_items WHERE order_id = ? ORDER BY id";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    long productId = rs.getLong("product_id");
                    item.setProductId(rs.wasNull() ? null : productId);
                    item.setProductName(rs.getString("product_name"));
                    item.setProductSku(rs.getString("product_sku"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<OrderStatusEvent> findHistory(Connection connection, long orderId) throws SQLException {
        String sql = """
                SELECT h.id, h.order_id, h.status, h.note, h.created_at, u.full_name AS changed_by_name
                FROM order_status_history h
                LEFT JOIN users u ON u.id = h.changed_by
                WHERE h.order_id = ? ORDER BY h.created_at, h.id
                """;
        List<OrderStatusEvent> events = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusEvent event = new OrderStatusEvent();
                    event.setId(rs.getLong("id"));
                    event.setOrderId(rs.getLong("order_id"));
                    event.setStatus(OrderStatus.fromString(rs.getString("status")));
                    event.setNote(rs.getString("note"));
                    event.setChangedByName(rs.getString("changed_by_name"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        event.setCreatedAt(created.toLocalDateTime());
                    }
                    events.add(event);
                }
            }
        }
        return events;
    }

    private long countAll(String where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders o JOIN users u ON u.id = o.user_id" + where;
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long countScalar(String sql, Object param) throws SQLException {
        try (Connection connection = Database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static Order map(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setUserId(rs.getLong("user_id"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setTotal(rs.getBigDecimal("total"));
        order.setCouponCode(rs.getString("coupon_code"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setRecipientName(rs.getString("recipient_name"));
        order.setPhone(rs.getString("phone"));
        order.setAddressLine1(rs.getString("address_line1"));
        order.setAddressLine2(rs.getString("address_line2"));
        order.setCity(rs.getString("city"));
        order.setState(rs.getString("state"));
        order.setPostalCode(rs.getString("postal_code"));
        order.setCountry(rs.getString("country"));
        order.setNotes(rs.getString("notes"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        Timestamp placed = rs.getTimestamp("placed_at");
        if (placed != null) {
            order.setPlacedAt(placed.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            order.setUpdatedAt(updated.toLocalDateTime());
        }
        return order;
    }
}
