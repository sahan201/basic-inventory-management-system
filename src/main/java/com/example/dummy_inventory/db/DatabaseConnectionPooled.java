package com.example.dummy_inventory.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection with Connection Pooling using HikariCP
 *
 * BENEFITS:
 * - Reuses existing connections instead of creating new ones
 * - Much better performance under load (10-100x faster than DriverManager)
 * - Automatic connection health checks
 * - Configurable pool size and timeouts
 * - Leak detection for debugging
 *
 * USAGE:
 * - Use exactly like DatabaseConnection: try (Connection conn = DatabaseConnectionPooled.getConnection())
 * - Call DatabaseConnectionPooled.shutdown() when application exits
 * - To switch entire application to pooled connections:
 *   1. Replace all imports of DatabaseConnection with DatabaseConnectionPooled
 *   2. Or, modify DatabaseConnection to use this implementation internally
 */
public class DatabaseConnectionPooled {

    private static HikariDataSource dataSource;
    private static boolean initialized = false;
    private static String initError = null;

    // Static initialization block - runs once when class is loaded
    static {
        try {
            initializeDataSource();
            initialized = true;
            System.out.println("✓ HikariCP connection pool initialized successfully");
        } catch (Exception e) {
            initError = e.getMessage();
            System.err.println("✗ Failed to initialize database connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize HikariCP data source with configuration from database.properties
     */
    private static void initializeDataSource() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnectionPooled.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new IOException("database.properties file not found in resources folder! " +
                        "Please create src/main/resources/database.properties");
            }

            props.load(input);
        }

        // Load database configuration
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String params = props.getProperty("db.connection.params", "");

        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url + params);
        config.setUsername(user);
        config.setPassword(password);

        // Pool configuration
        config.setMaximumPoolSize(10);              // Maximum number of connections
        config.setMinimumIdle(2);                   // Minimum idle connections
        config.setIdleTimeout(300000);              // 5 minutes - close idle connections
        config.setConnectionTimeout(20000);         // 20 seconds - max wait for connection
        config.setMaxLifetime(1200000);             // 20 minutes - max connection lifetime

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Connection testing
        config.setConnectionTestQuery("SELECT 1");

        // Pool name for monitoring
        config.setPoolName("InventoryManagementPool");

        // Create the data source
        dataSource = new HikariDataSource(config);
    }

    /**
     * Get a connection from the pool
     * ALWAYS use try-with-resources to ensure connection is returned to pool:
     *
     * try (Connection conn = DatabaseConnectionPooled.getConnection()) {
     *     // Use connection
     * } // Connection automatically returned to pool here
     *
     * @return Connection from the pool
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database connection pool not initialized: " + initError);
        }
        return dataSource.getConnection();
    }

    /**
     * Check if connection pool is available and ready
     *
     * @return true if pool is initialized and ready
     */
    public static boolean isAvailable() {
        return initialized && dataSource != null && !dataSource.isClosed();
    }

    /**
     * Test if we can get a valid connection from the pool
     *
     * @return true if connection test succeeds
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shutdown the connection pool - call on application exit
     * IMPORTANT: Call this in your application's stop() method or shutdown hook
     *
     * Example in JavaFX Application:
     * @Override
     * public void stop() {
     *     DatabaseConnectionPooled.shutdown();
     *     super.stop();
     * }
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Database connection pool shut down successfully");
        }
    }

    /**
     * Get pool statistics for monitoring and debugging
     *
     * @return String with current pool statistics
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "Pool not initialized";
        }
        try {
            return String.format("Pool Stats: Active=%d, Idle=%d, Total=%d, Waiting=%d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        } catch (Exception e) {
            return "Unable to get pool stats: " + e.getMessage();
        }
    }

    /**
     * Main method to test connection pooling
     */
    public static void main(String[] args) {
        System.out.println("Testing HikariCP connection pool...");
        System.out.println("===================================");

        // Test 1: Check initialization
        if (isAvailable()) {
            System.out.println("✓ Pool initialized successfully");
            System.out.println(getPoolStats());
        } else {
            System.out.println("✗ Pool initialization failed");
            return;
        }

        // Test 2: Get a connection
        System.out.println("\nTesting connection retrieval...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connection obtained successfully");
                System.out.println("  Database: " + conn.getCatalog());
                System.out.println("  Valid: " + conn.isValid(2));
                System.out.println("\n" + getPoolStats());
            }
        } catch (SQLException e) {
            System.out.println("✗ Failed to get connection: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: Test multiple concurrent connections
        System.out.println("\nTesting concurrent connections...");
        try {
            Connection conn1 = getConnection();
            Connection conn2 = getConnection();
            Connection conn3 = getConnection();

            System.out.println("✓ Multiple connections obtained");
            System.out.println(getPoolStats());

            conn1.close();
            conn2.close();
            conn3.close();

            System.out.println("\nAfter closing connections:");
            System.out.println(getPoolStats());

        } catch (SQLException e) {
            System.out.println("✗ Concurrent connection test failed: " + e.getMessage());
        }

        // Test 4: Shutdown
        System.out.println("\nShutting down pool...");
        shutdown();

        System.out.println("===================================");
    }
}
