package com.example.dummy_inventory.dao;
import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ProductDAO {
    public boolean createProduct(Product product) {
        String sql = "INSERT INTO Product (name, quantity_in_stock, price, category_id, supplier_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getQuantityInStock());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setInt(5, product.getSupplierId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating product:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.quantity_in_stock, p.price, " +
                "p.category_id, p.supplier_id, c.name AS category_name, s.name AS supplier_name " +
                "FROM Product p " +
                "JOIN Category c ON p.category_id = c.category_id " +
                "JOIN Supplier s ON p.supplier_id = s.supplier_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getInt("quantity_in_stock"),
                        rs.getDouble("price"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id")
                );
                product.setCategoryName(rs.getString("category_name"));
                product.setSupplierName(rs.getString("supplier_name"));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error getting products:");
            e.printStackTrace();
        }

        return products;
    }

    public Product getProductById(int productId) {
        String sql = "SELECT p.product_id, p.name, p.quantity_in_stock, p.price, " +
                "p.category_id, p.supplier_id, c.name AS category_name, s.name AS supplier_name " +
                "FROM Product p " +
                "JOIN Category c ON p.category_id = c.category_id " +
                "JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                "WHERE p.product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getInt("quantity_in_stock"),
                            rs.getDouble("price"),
                            rs.getInt("category_id"),
                            rs.getInt("supplier_id")
                    );
                    product.setCategoryName(rs.getString("category_name"));
                    product.setSupplierName(rs.getString("supplier_name"));
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting product:");
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE Product SET name = ?, quantity_in_stock = ?, price = ?, category_id = ?, supplier_id = ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getQuantityInStock());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setInt(5, product.getSupplierId());
            pstmt.setInt(6, product.getProductId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating product:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Product WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting product:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> searchProducts(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.quantity_in_stock, p.price, " +
                "p.category_id, p.supplier_id, c.name AS category_name, s.name AS supplier_name " +
                "FROM Product p " +
                "JOIN Category c ON p.category_id = c.category_id " +
                "JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                "WHERE p.name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getInt("quantity_in_stock"),
                            rs.getDouble("price"),
                            rs.getInt("category_id"),
                            rs.getInt("supplier_id")
                    );
                    product.setCategoryName(rs.getString("category_name"));
                    product.setSupplierName(rs.getString("supplier_name"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching products:");
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getLowStockProducts(int threshold) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.quantity_in_stock, p.price, " +
                "p.category_id, p.supplier_id, c.name AS category_name, s.name AS supplier_name " +
                "FROM Product p " +
                "JOIN Category c ON p.category_id = c.category_id " +
                "JOIN Supplier s ON p.supplier_id = s.supplier_id " +
                "WHERE p.quantity_in_stock < ? " +
                "ORDER BY p.quantity_in_stock ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getInt("quantity_in_stock"),
                            rs.getDouble("price"),
                            rs.getInt("category_id"),
                            rs.getInt("supplier_id")
                    );
                    product.setCategoryName(rs.getString("category_name"));
                    product.setSupplierName(rs.getString("supplier_name"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock products:");
            e.printStackTrace();
        }

        return products;
    }

}
