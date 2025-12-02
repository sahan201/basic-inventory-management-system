package com.example.dummy_inventory.dao;
import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.Sale;
import com.example.dummy_inventory.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class SaleDAO {
    private ProductDAO productDAO = new ProductDAO();

    public boolean createSale(Sale sale) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Fetch product to get current price and check stock
                Product product = productDAO.getProductById(sale.getProductId());
                if (product == null) {
                    System.err.println("Product not found");
                    conn.rollback();
                    return false;
                }

                // Check stock availability
                if (product.getQuantityInStock() < sale.getQuantitySold()) {
                    System.err.println("Insufficient stock. Available: " + product.getQuantityInStock());
                    conn.rollback();
                    return false;
                }

                // Set unit price from product if not already set
                if (sale.getUnitPrice() == 0) {
                    sale.setUnitPrice(product.getPrice());
                }

                // Calculate total amount
                double totalAmount = sale.getQuantitySold() * sale.getUnitPrice();
                sale.setTotalAmount(totalAmount);

                // Insert sale with all required fields
                String insertSql = "INSERT INTO Sale (product_id, quantity_sold, unit_price, total_amount, sale_date, user_id, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, sale.getProductId());
                    pstmt.setInt(2, sale.getQuantitySold());
                    pstmt.setDouble(3, sale.getUnitPrice());
                    pstmt.setDouble(4, sale.getTotalAmount());
                    pstmt.setTimestamp(5, Timestamp.valueOf(sale.getSaleDate()));

                    // Handle nullable user_id
                    if (sale.getUserId() != null) {
                        pstmt.setInt(6, sale.getUserId());
                    } else {
                        pstmt.setNull(6, java.sql.Types.INTEGER);
                    }

                    // Handle payment method
                    if (sale.getPaymentMethod() != null) {
                        pstmt.setString(7, sale.getPaymentMethod().name());
                    } else {
                        pstmt.setString(7, Sale.PaymentMethod.CASH.name());
                    }

                    // Handle nullable notes
                    if (sale.getNotes() != null) {
                        pstmt.setString(8, sale.getNotes());
                    } else {
                        pstmt.setNull(8, java.sql.Types.VARCHAR);
                    }

                    pstmt.executeUpdate();
                }

                // Update product stock
                String updateSql = "UPDATE Product SET quantity_in_stock = quantity_in_stock - ? WHERE product_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, sale.getQuantitySold());
                    pstmt.setInt(2, sale.getProductId());
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                System.err.println("Error creating sale:");
                e.printStackTrace();
                conn.rollback();
                return false;
            } finally {
                // Restore autocommit to default state before connection is closed
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring autocommit:");
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error with database connection:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.unit_price, s.total_amount, " +
                "s.sale_date, s.user_id, s.payment_method, s.notes, " +
                "p.name AS product_name, p.price " +
                "FROM Sale s " +
                "JOIN Product p ON s.product_id = p.product_id " +
                "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getInt("sale_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("sale_date").toLocalDateTime(),
                        (Integer) rs.getObject("user_id"),
                        rs.getString("payment_method") != null ?
                                Sale.PaymentMethod.valueOf(rs.getString("payment_method")) : Sale.PaymentMethod.CASH,
                        rs.getString("notes")
                );
                sale.setProductName(rs.getString("product_name"));
                sale.setProductPrice(rs.getDouble("price"));
                sales.add(sale);
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales:");
            e.printStackTrace();
        }

        return sales;
    }

    public Sale getSaleById(int saleId) {
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.unit_price, s.total_amount, " +
                "s.sale_date, s.user_id, s.payment_method, s.notes, " +
                "p.name AS product_name, p.price " +
                "FROM Sale s " +
                "JOIN Product p ON s.product_id = p.product_id " +
                "WHERE s.sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale(
                            rs.getInt("sale_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity_sold"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("sale_date").toLocalDateTime(),
                            (Integer) rs.getObject("user_id"),
                            rs.getString("payment_method") != null ?
                                    Sale.PaymentMethod.valueOf(rs.getString("payment_method")) : Sale.PaymentMethod.CASH,
                            rs.getString("notes")
                    );
                    sale.setProductName(rs.getString("product_name"));
                    sale.setProductPrice(rs.getDouble("price"));
                    return sale;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting sale:");
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteSale(int saleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // First, get the sale details to restore stock
                String getSaleSql = "SELECT product_id, quantity_sold FROM Sale WHERE sale_id = ?";
                int productId = 0;
                int quantitySold = 0;

                try (PreparedStatement pstmt = conn.prepareStatement(getSaleSql)) {
                    pstmt.setInt(1, saleId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            productId = rs.getInt("product_id");
                            quantitySold = rs.getInt("quantity_sold");
                        } else {
                            // Sale not found
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Restore stock to product
                String updateStockSql = "UPDATE Product SET quantity_in_stock = quantity_in_stock + ? WHERE product_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
                    pstmt.setInt(1, quantitySold);
                    pstmt.setInt(2, productId);
                    pstmt.executeUpdate();
                }

                // Delete the sale
                String deleteSql = "DELETE FROM Sale WHERE sale_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setInt(1, saleId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                System.err.println("Error deleting sale:");
                e.printStackTrace();
                conn.rollback();
                return false;
            } finally {
                // Restore autocommit to default state before connection is closed
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring autocommit:");
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error with database connection:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.unit_price, s.total_amount, " +
                "s.sale_date, s.user_id, s.payment_method, s.notes, " +
                "p.name AS product_name, p.price " +
                "FROM Sale s " +
                "JOIN Product p ON s.product_id = p.product_id " +
                "WHERE s.sale_date BETWEEN ? AND ? " +
                "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale(
                            rs.getInt("sale_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity_sold"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("sale_date").toLocalDateTime(),
                            (Integer) rs.getObject("user_id"),
                            rs.getString("payment_method") != null ?
                                    Sale.PaymentMethod.valueOf(rs.getString("payment_method")) : Sale.PaymentMethod.CASH,
                            rs.getString("notes")
                    );
                    sale.setProductName(rs.getString("product_name"));
                    sale.setProductPrice(rs.getDouble("price"));
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales by date range:");
            e.printStackTrace();
        }

        return sales;
    }

    public double getTotalRevenue() {
        // Use total_amount from Sale table for accurate revenue calculation
        // This accounts for the actual price at time of sale, not current product price
        String sql = "SELECT SUM(total_amount) AS total_revenue FROM Sale";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue:");
            e.printStackTrace();
        }

        return 0.0;
    }

}
