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
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Product product = productDAO.getProductById(sale.getProductId());
            if (product == null) {
                System.err.println("Product not found");
                conn.rollback();
                return false;
            }

            if (product.getQuantityInStock() < sale.getQuantitySold()) {
                System.err.println("Insufficient stock. Available: " + product.getQuantityInStock());
                conn.rollback();
                return false;
            }

            String insertSql = "INSERT INTO Sale (product_id, quantity_sold, sale_date) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, sale.getProductId());
                pstmt.setInt(2, sale.getQuantitySold());
                pstmt.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
                pstmt.executeUpdate();
            }

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.sale_date, " +
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
                        rs.getTimestamp("sale_date").toLocalDateTime()
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
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.sale_date, " +
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
                            rs.getTimestamp("sale_date").toLocalDateTime()
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
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.product_id, s.quantity_sold, s.sale_date, " +
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
                            rs.getTimestamp("sale_date").toLocalDateTime()
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
        String sql = "SELECT SUM(s.quantity_sold * p.price) AS total_revenue " +
                "FROM Sale s " +
                "JOIN Product p ON s.product_id = p.product_id";

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
