package com.example.dummy_inventory.dao;
import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Paste this inside UserDAO.java, replacing the existing login method

public User login(String username, String password) {
    // We removed 'AND is_active = TRUE' to debug if the user exists but is inactive
    String sql = "SELECT user_id, username, password, role, full_name, email, is_active, " +
                 "created_at, last_login FROM User WHERE username = ?";

    System.out.println("--- DEBUG LOGIN START ---");
    System.out.println("Testing username: '" + username + "'");
    System.out.println("Testing password: '" + password + "'");

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("DEBUG: User found in database!");

                String dbHash = rs.getString("password");
                boolean isActive = rs.getBoolean("is_active");
                
                System.out.println("DEBUG: DB Hash: " + dbHash);
                System.out.println("DEBUG: Is Active: " + isActive);

                // Check 1: Is user active?
                if (!isActive) {
                    System.err.println("LOGIN FAIL: User exists but is_active is false");
                    return null;
                }

                // Check 2: Does password match?
                boolean passwordMatch = BCrypt.checkpw(password, dbHash);
                System.out.println("DEBUG: Password Match Result: " + passwordMatch);

                if (passwordMatch) {
                    System.out.println("LOGIN SUCCESS: Credentials valid.");
                    
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        dbHash,
                        User.Role.valueOf(rs.getString("role")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("last_login") != null ?
                            rs.getTimestamp("last_login").toLocalDateTime() : null
                    );
                    updateLastLogin(user.getUserId());
                    return user;
                } else {
                    System.err.println("LOGIN FAIL: Password hash mismatch.");
                }
            } else {
                System.err.println("LOGIN FAIL: Username '" + username + "' not found in database.");
            }
        }
    } catch (SQLException e) {
        System.err.println("ERROR: Database error during login");
        e.printStackTrace();
    }
    System.out.println("--- DEBUG LOGIN END ---");
    return null;
}

    private void updateLastLogin(int userId) {
        String sql = "UPDATE User SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login:");
            e.printStackTrace();
        }
    }

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO User (username, password, role, full_name, email, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashPassword(user.getPassword()));
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setBoolean(6, user.isActive());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating user:");
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, full_name, email, is_active, created_at, last_login FROM User ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        User.Role.valueOf(rs.getString("role")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users:");
            e.printStackTrace();
        }

        return users;
    }

    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, password, role, full_name, email, is_active, created_at, last_login FROM User WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            User.Role.valueOf(rs.getString("role")),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                            rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID:");
            e.printStackTrace();
        }

        return null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, password, role, full_name, email, is_active, created_at, last_login FROM User WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            User.Role.valueOf(rs.getString("role")),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                            rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username:");
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE User SET username = ?, role = ?, full_name = ?, email = ?, is_active = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole().name());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setInt(6, user.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE User SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashPassword(newPassword));
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating password:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM User WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user:");
            e.printStackTrace();
            return false;
        }
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, full_name, email, is_active, created_at, last_login FROM User WHERE username LIKE ? OR full_name LIKE ? ORDER BY username";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String search = "%" + searchTerm + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            User.Role.valueOf(rs.getString("role")),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                            rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching users:");
            e.printStackTrace();
        }

        return users;
    }

    public List<User> getUsersByRole(User.Role role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, full_name, email, is_active, created_at, last_login FROM User WHERE role = ? ORDER BY username";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            User.Role.valueOf(rs.getString("role")),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("is_active"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                            rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting users by role:");
            e.printStackTrace();
        }

        return users;
    }

    public boolean setUserActive(int userId, boolean isActive) {
        String sql = "UPDATE User SET is_active = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user status:");
            e.printStackTrace();
            return false;
        }
    }
}
