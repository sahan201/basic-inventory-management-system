package com.example.dummy_inventory.dao;
import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public boolean createCategory(Category category) {
        String sql = "INSERT INTO Category (name, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating category:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, description FROM Category";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories:");
            e.printStackTrace();
        }

        return categories;
    }

    public Category getCategoryById(int categoryId) {
        String sql = "SELECT category_id, name, description FROM Category WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting category:");
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateCategory(Category category) {
        String sql = "UPDATE Category SET name = ?, description = ? WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getCategoryId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating category:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM Category WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting category:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Category> searchCategories(String searchTerm) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, description FROM Category WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching categories:");
            e.printStackTrace();
        }

        return categories;
    }

}
