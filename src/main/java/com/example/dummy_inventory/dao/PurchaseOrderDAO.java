package com.example.dummy_inventory.dao;

import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.PurchaseOrder;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    /**
     * Create a new purchase order
     */
    public boolean createPurchaseOrder(PurchaseOrder order) {
        String sql = "INSERT INTO PurchaseOrder (supplier_id, product_id, quantity, unit_cost, " +
                     "total_cost, expected_delivery, status, user_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, order.getSupplierId());
            pstmt.setInt(2, order.getProductId());
            pstmt.setInt(3, order.getQuantity());
            pstmt.setDouble(4, order.getUnitCost());
            pstmt.setDouble(5, order.getTotalCost());
            pstmt.setDate(6, order.getExpectedDelivery() != null ?
                    Date.valueOf(order.getExpectedDelivery()) : null);
            pstmt.setString(7, order.getStatus().name());

            if (order.getUserId() != null) {
                pstmt.setInt(8, order.getUserId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setString(9, order.getNotes());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get generated order ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error creating purchase order:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all purchase orders with supplier and product names
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT po.*, s.name AS supplier_name, p.name AS product_name, u.username " +
                     "FROM PurchaseOrder po " +
                     "JOIN Supplier s ON po.supplier_id = s.supplier_id " +
                     "JOIN Product p ON po.product_id = p.product_id " +
                     "LEFT JOIN User u ON po.user_id = u.user_id " +
                     "ORDER BY po.order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PurchaseOrder order = extractPurchaseOrderFromResultSet(rs);
                order.setSupplierName(rs.getString("supplier_name"));
                order.setProductName(rs.getString("product_name"));
                order.setUserName(rs.getString("username"));
                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all purchase orders:");
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Get purchase order by ID
     */
    public PurchaseOrder getPurchaseOrderById(int orderId) {
        String sql = "SELECT po.*, s.name AS supplier_name, p.name AS product_name, u.username " +
                     "FROM PurchaseOrder po " +
                     "JOIN Supplier s ON po.supplier_id = s.supplier_id " +
                     "JOIN Product p ON po.product_id = p.product_id " +
                     "LEFT JOIN User u ON po.user_id = u.user_id " +
                     "WHERE po.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrder order = extractPurchaseOrderFromResultSet(rs);
                    order.setSupplierName(rs.getString("supplier_name"));
                    order.setProductName(rs.getString("product_name"));
                    order.setUserName(rs.getString("username"));
                    return order;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting purchase order by ID:");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update purchase order status
     */
    public boolean updateOrderStatus(int orderId, PurchaseOrder.Status status, LocalDateTime receivedDate) {
        String sql = "UPDATE PurchaseOrder SET status = ?, received_date = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            if (receivedDate != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(receivedDate));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }

            pstmt.setInt(3, orderId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating order status:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark order as received and update product stock
     */
    public boolean receiveOrder(int orderId) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get order details
            PurchaseOrder order = getPurchaseOrderById(orderId);
            if (order == null || order.getStatus() != PurchaseOrder.Status.PENDING) {
                conn.rollback();
                return false;
            }

            // Update order status
            String updateOrderSql = "UPDATE PurchaseOrder SET status = 'RECEIVED', received_date = CURRENT_TIMESTAMP WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderSql)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // Update product stock
            String updateStockSql = "UPDATE Product SET quantity_in_stock = quantity_in_stock + ? WHERE product_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
                pstmt.setInt(1, order.getQuantity());
                pstmt.setInt(2, order.getProductId());
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error receiving order:");
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get orders by status
     */
    public List<PurchaseOrder> getOrdersByStatus(PurchaseOrder.Status status) {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT po.*, s.name AS supplier_name, p.name AS product_name, u.username " +
                     "FROM PurchaseOrder po " +
                     "JOIN Supplier s ON po.supplier_id = s.supplier_id " +
                     "JOIN Product p ON po.product_id = p.product_id " +
                     "LEFT JOIN User u ON po.user_id = u.user_id " +
                     "WHERE po.status = ? " +
                     "ORDER BY po.order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrder order = extractPurchaseOrderFromResultSet(rs);
                    order.setSupplierName(rs.getString("supplier_name"));
                    order.setProductName(rs.getString("product_name"));
                    order.setUserName(rs.getString("username"));
                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting orders by status:");
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Delete purchase order
     */
    public boolean deletePurchaseOrder(int orderId) {
        String sql = "DELETE FROM PurchaseOrder WHERE order_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting purchase order:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extract PurchaseOrder object from ResultSet
     */
    private PurchaseOrder extractPurchaseOrderFromResultSet(ResultSet rs) throws SQLException {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderId(rs.getInt("order_id"));
        order.setSupplierId(rs.getInt("supplier_id"));
        order.setProductId(rs.getInt("product_id"));
        order.setQuantity(rs.getInt("quantity"));
        order.setUnitCost(rs.getDouble("unit_cost"));
        order.setTotalCost(rs.getDouble("total_cost"));

        Timestamp orderTimestamp = rs.getTimestamp("order_date");
        if (orderTimestamp != null) {
            order.setOrderDate(orderTimestamp.toLocalDateTime());
        }

        Date expectedDeliveryDate = rs.getDate("expected_delivery");
        if (expectedDeliveryDate != null) {
            order.setExpectedDelivery(expectedDeliveryDate.toLocalDate());
        }

        order.setStatus(PurchaseOrder.Status.valueOf(rs.getString("status")));

        Timestamp receivedTimestamp = rs.getTimestamp("received_date");
        if (receivedTimestamp != null) {
            order.setReceivedDate(receivedTimestamp.toLocalDateTime());
        }

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            order.setUserId(userId);
        }

        order.setNotes(rs.getString("notes"));

        return order;
    }
}
