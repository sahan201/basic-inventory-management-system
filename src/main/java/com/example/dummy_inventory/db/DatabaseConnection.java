package com.example.dummy_inventory.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection class manages MySQL database connections
 * Uses Singleton pattern to ensure only one connection instance
 */
public class DatabaseConnection {

    // Database connection details
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/inventory_management";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "2001@sahan";

    // Optional: Add these parameters to the URL for better connection handling
    private static final String CONNECTION_PARAMS = "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String FULL_URL = DATABASE_URL + CONNECTION_PARAMS;

    // Singleton instance
    private static Connection connection = null;

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Returns a database connection
     * Creates a new connection if one doesn't exist
     *
     * @return Connection object or null if connection fails
     */
    public static Connection getConnection() {
        try {
            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC Driver (optional for newer JDBC versions)
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish connection using DriverManager
                connection = DriverManager.getConnection(FULL_URL, DATABASE_USER, DATABASE_PASSWORD);

                System.out.println("Database connection established successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            System.err.println("Make sure mysql-connector-java is in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed! Error: " + e.getMessage());
            System.err.println("Check your database URL, username, and password.");
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Creates a new connection each time (for concurrent operations)
     * Use this if you need multiple simultaneous connections
     *
     * @return New Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getNewConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(FULL_URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests if the connection is valid
     *
     * @return true if connection is valid, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Main method to test database connection
     */
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        System.out.println("================================");

        Connection conn = getConnection();

        if (conn != null) {
            try {
                // Get connection metadata
                System.out.println("✓ Database connection successful!");
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("User: " + conn.getMetaData().getUserName());
                System.out.println("Driver: " + conn.getMetaData().getDriverName());
                System.out.println("Driver Version: " + conn.getMetaData().getDriverVersion());

                // Don't close in main - let closeConnection() handle it
                // Or you can close it here for testing
                // conn.close();
            } catch (SQLException e) {
                System.err.println("Error getting connection info: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("✗ Failed to make connection.");
            System.out.println("\nTroubleshooting steps:");
            System.out.println("1. Make sure MySQL server is running");
            System.out.println("2. Check database name: inventory_management exists");
            System.out.println("3. Verify username: " + DATABASE_USER);
            System.out.println("4. Verify password is correct");
            System.out.println("5. Ensure MySQL Connector JAR is in classpath");
        }

        System.out.println("================================");
    }
}

/*
============================================
SETUP INSTRUCTIONS
============================================

1. ADD MYSQL CONNECTOR TO YOUR PROJECT:

   If using Maven, add to pom.xml:
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <version>8.2.0</version>
   </dependency>

   If using Gradle, add to build.gradle:
   implementation 'com.mysql:mysql-connector-j:8.2.0'

   If manual setup:
   - Download mysql-connector-java JAR
   - Add to project classpath/libraries

2. VERIFY DATABASE:
   - MySQL server is running
   - Database 'inventory_management' exists
   - Username and password are correct

3. COMMON ISSUES:

   Issue: "No suitable driver found"
   Solution: Add MySQL Connector JAR to project

   Issue: "Access denied for user"
   Solution: Check username/password, grant privileges

   Issue: "Unknown database"
   Solution: Run the SQL schema script first

   Issue: "Communications link failure"
   Solution: Check MySQL server is running on port 3306

============================================
USAGE IN DAO CLASSES
============================================

Example:
--------
import com.example.dummy_inventory.db.DatabaseConnection;

public class UserDAO {
    public User login(String username, String password) {
        String sql = "SELECT * FROM User WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

============================================
*/