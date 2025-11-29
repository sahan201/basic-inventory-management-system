package com.example.dummy_inventory.dao;
import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    public boolean createSupplier(Supplier supplier) {
        String sql = "INSERT INTO Supplier (name, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating supplier:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT supplier_id, name, contact_person, email, phone, address FROM Supplier";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting suppliers:");
            e.printStackTrace();
        }

        return suppliers;
    }

    public Supplier getSupplierById(int supplierId) {
        String sql = "SELECT supplier_id, name, contact_person, email, phone, address FROM Supplier WHERE supplier_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supplierId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getString("contact_person"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting supplier:");
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE Supplier SET name = ?, contact_person = ?, email = ?, phone = ?, address = ? WHERE supplier_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setInt(6, supplier.getSupplierId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating supplier:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM Supplier WHERE supplier_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supplierId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting supplier:");
            e.printStackTrace();
            return false;
        }
    }

    public List<Supplier> searchSuppliers(String searchTerm) {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT supplier_id, name, contact_person, email, phone, address FROM Supplier WHERE name LIKE ? OR contact_person LIKE ? OR phone LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String search = "%" + searchTerm + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getString("contact_person"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching suppliers:");
            e.printStackTrace();
        }

        return suppliers;
    }

}
