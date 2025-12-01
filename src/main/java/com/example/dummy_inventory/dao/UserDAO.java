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
    String sql = "SELECT user_id, username, password, role, full_name, email, is_active, " +
                 "created_at, last_login FROM User WHERE username = ? AND is_active = TRUE";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String dbHash = rs.getString("password");

                // Check password match
                if (BCrypt.checkpw(password, dbHash)) {
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
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error during login:");
        e.printStackTrace();
    }
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

    public boolean createUser(User user) throws SQLException {
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
            // Re-throw to allow caller to handle specific error cases
            throw e;
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
