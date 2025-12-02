package com.example.dummy_inventory.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection class manages MySQL database connections
 * Uses Singleton pattern to ensure only one connection instance
 * Reads database credentials from database.properties file
 */
public class DatabaseConnection {

    // Database connection details - loaded from properties file
    private static final String DATABASE_URL;
    private static final String DATABASE_USER;
    private static final String DATABASE_PASSWORD;
    private static final String FULL_URL;

    // Static block to load database configuration from properties file
    static {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                System.err.println("ERROR: database.properties file not found in resources folder!");
                System.err.println("Please create src/main/resources/database.properties");
                throw new RuntimeException("database.properties file not found");
            }

            props.load(input);

            DATABASE_URL = props.getProperty("db.url");
            DATABASE_USER = props.getProperty("db.user");
            DATABASE_PASSWORD = props.getProperty("db.password");
            String connectionParams = props.getProperty("db.connection.params", "");
            FULL_URL = DATABASE_URL + connectionParams;

            System.out.println("Database configuration loaded successfully from database.properties");

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load database configuration!");
            e.printStackTrace();
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Returns a new database connection
     * Creates a fresh connection for each call to work properly with try-with-resources
     *
     * NOTE: The Singleton pattern was removed because DAOs use try-with-resources,
     * which automatically closes connections. This was causing the singleton connection
     * to be closed and reopened on every query, defeating the purpose.
     *
     * For production use, consider implementing a proper connection pool (e.g., HikariCP)
     * instead of creating new connections on every call.
     *
     * @return Connection object or null if connection fails
     */
    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver (optional for newer JDBC versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection using DriverManager
            Connection conn = DriverManager.getConnection(FULL_URL, DATABASE_USER, DATABASE_PASSWORD);

            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            System.err.println("Make sure mysql-connector-java is in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed! Error: " + e.getMessage());
            System.err.println("Check your database URL, username, and password.");
            e.printStackTrace();
        }

        return null;
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
     * Tests if the connection is valid
     *
     * @return true if connection is valid, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
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
            System.out.println("2. Check database name exists in database.properties");
            System.out.println("3. Verify credentials in database.properties");
            System.out.println("4. Ensure database.properties is in src/main/resources/");
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