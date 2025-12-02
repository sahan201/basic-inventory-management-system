package com.example.dummy_inventory.dao;

import com.example.dummy_inventory.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ReportsDAO {

    // Sales Reports
    
    public double getTotalRevenue() {
        // Use persisted total_amount for consistency with Sale records
        String sql = "SELECT SUM(s.total_amount) AS total_revenue FROM Sale s";

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
    
    public double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Use persisted total_amount for consistency with Sale records
        String sql = "SELECT SUM(s.total_amount) AS revenue FROM Sale s WHERE s.sale_date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("revenue");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting revenue by date range:");
            e.printStackTrace();
        }

        return 0.0;
    }
    
    public Map<String, Object> getTopSellingProducts(int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        String sql = "SELECT p.name, SUM(s.quantity_sold) AS total_sold, SUM(s.total_amount) AS revenue " +
                     "FROM Sale s JOIN Product p ON s.product_id = p.product_id " +
                     "GROUP BY p.product_id ORDER BY total_sold DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("total_sold", rs.getInt("total_sold"));
                    productData.put("revenue", rs.getDouble("revenue"));
                    result.put(rs.getString("name"), productData);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting top selling products:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    public Map<String, Double> getSalesByCategory() {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT c.name, SUM(s.total_amount) AS revenue " +
                     "FROM Sale s " +
                     "JOIN Product p ON s.product_id = p.product_id " +
                     "JOIN Category c ON p.category_id = c.category_id " +
                     "GROUP BY c.category_id ORDER BY revenue DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                result.put(rs.getString("name"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales by category:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Inventory Reports
    
    public double getTotalInventoryValue() {
        String sql = "SELECT SUM(quantity_in_stock * price) AS total_value FROM Product";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total_value");
            }
        } catch (SQLException e) {
            System.err.println("Error getting inventory value:");
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public List<Map<String, Object>> getLowStockReport(int threshold) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT p.name, p.quantity_in_stock, p.reorder_level, c.name AS category, s.name AS supplier " +
                     "FROM Product p " +
                     "JOIN Category c ON p.category_id = c.category_id " +
                     "JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                     "WHERE p.quantity_in_stock < ? ORDER BY p.quantity_in_stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, threshold);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("product_name", rs.getString("name"));
                    item.put("stock", rs.getInt("quantity_in_stock"));
                    item.put("reorder_level", rs.getInt("reorder_level"));
                    item.put("category", rs.getString("category"));
                    item.put("supplier", rs.getString("supplier"));
                    result.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock report:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    public Map<String, Integer> getProductCountByCategory() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT c.name, COUNT(p.product_id) AS product_count " +
                     "FROM Category c " +
                     "LEFT JOIN Product p ON c.category_id = p.category_id " +
                     "GROUP BY c.category_id ORDER BY product_count DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                result.put(rs.getString("name"), rs.getInt("product_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting product count by category:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Dashboard Statistics
    
    public int getTotalProductCount() {
        String sql = "SELECT COUNT(*) AS count FROM Product";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting product count:");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int getTotalCategoryCount() {
        String sql = "SELECT COUNT(*) AS count FROM Category";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting category count:");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int getTotalSupplierCount() {
        String sql = "SELECT COUNT(*) AS count FROM Supplier";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting supplier count:");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int getTotalSalesCount() {
        String sql = "SELECT COUNT(*) AS count FROM Sale";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales count:");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int getLowStockProductCount(int threshold) {
        String sql = "SELECT COUNT(*) AS count FROM Product WHERE quantity_in_stock < ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, threshold);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock count:");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // Time-based Reports
    
    public Map<String, Double> getDailySalesReport(LocalDate date) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(sale_date) AS sale_day, SUM(total_amount) AS daily_revenue " +
                     "FROM Sale WHERE DATE(sale_date) = ? GROUP BY DATE(sale_date)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.put(rs.getDate("sale_day").toString(), rs.getDouble("daily_revenue"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily sales:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    public Map<String, Double> getMonthlySalesReport(int year, int month) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(sale_date) AS sale_day, SUM(total_amount) AS daily_revenue " +
                     "FROM Sale WHERE YEAR(sale_date) = ? AND MONTH(sale_date) = ? " +
                     "GROUP BY DATE(sale_date) ORDER BY sale_day";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getDate("sale_day").toString(), rs.getDouble("daily_revenue"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting monthly sales:");
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Profit Analysis (requires cost_price field)
    
    public double getTotalProfit() {
        String sql = "SELECT SUM(s.quantity_sold * (s.unit_price - COALESCE(p.cost_price, 0))) AS profit " +
                     "FROM Sale s JOIN Product p ON s.product_id = p.product_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("profit");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total profit:");
            e.printStackTrace();
        }
        
        return 0.0;
    }
}
