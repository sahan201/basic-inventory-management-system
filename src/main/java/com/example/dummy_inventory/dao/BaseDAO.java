package com.example.dummy_inventory.dao;

import com.example.dummy_inventory.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base DAO class providing common database operations
 * All DAOs should extend this class for consistent error handling and connection management
 *
 * BENEFITS:
 * - Reduces code duplication across DAOs
 * - Consistent error handling and logging
 * - Proper resource management with try-with-resources
 * - Type-safe result mapping with functional interfaces
 */
public abstract class BaseDAO {

    /**
     * Execute a query and map results to objects
     *
     * @param sql SQL query to execute
     * @param mapper Function to map ResultSet to object
     * @param params Query parameters
     * @return List of mapped objects
     */
    protected <T> List<T> executeQuery(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            logError("Query execution failed", sql, e);
        }

        return results;
    }

    /**
     * Execute a query expecting a single result
     *
     * @param sql SQL query to execute
     * @param mapper Function to map ResultSet to object
     * @param params Query parameters
     * @return Optional containing result or empty if not found
     */
    protected <T> Optional<T> executeSingleQuery(String sql, ResultSetMapper<T> mapper, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            logError("Single query execution failed", sql, e);
        }

        return Optional.empty();
    }

    /**
     * Execute an update (INSERT, UPDATE, DELETE)
     *
     * @param sql SQL statement to execute
     * @param params Statement parameters
     * @return true if at least one row was affected
     */
    protected boolean executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logError("Update execution failed", sql, e);
            return false;
        }
    }

    /**
     * Execute an insert and return generated key
     *
     * @param sql INSERT SQL statement
     * @param params Statement parameters
     * @return Optional containing generated key or empty if failed
     */
    protected Optional<Integer> executeInsertWithKey(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(pstmt, params);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return Optional.of(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logError("Insert with key failed", sql, e);
        }

        return Optional.empty();
    }

    /**
     * Execute operations within a transaction
     *
     * @param callback Function to execute within transaction
     * @return Optional containing result or empty if transaction failed
     */
    protected <T> Optional<T> executeInTransaction(TransactionCallback<T> callback) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            T result = callback.execute(conn);
            conn.commit();
            return Optional.ofNullable(result);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logError("Transaction rollback failed", "", rollbackEx);
                }
            }
            logError("Transaction failed", "", e);
            return Optional.empty();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logError("Connection cleanup failed", "", e);
                }
            }
        }
    }

    /**
     * Execute a count query
     *
     * @param sql COUNT SQL query
     * @param params Query parameters
     * @return Count result or 0 if failed
     */
    protected int executeCount(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("Count query failed", sql, e);
        }

        return 0;
    }

    /**
     * Execute a sum query
     *
     * @param sql SUM SQL query
     * @param params Query parameters
     * @return Sum result or 0.0 if failed
     */
    protected double executeSum(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            logError("Sum query failed", sql, e);
        }

        return 0.0;
    }

    /**
     * Set parameters on a PreparedStatement with proper type handling
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;

            if (param == null) {
                pstmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                pstmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                pstmt.setLong(index, (Long) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(index, (Double) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof java.sql.Date) {
                pstmt.setDate(index, (java.sql.Date) param);
            } else if (param instanceof java.sql.Timestamp) {
                pstmt.setTimestamp(index, (java.sql.Timestamp) param);
            } else if (param instanceof java.time.LocalDateTime) {
                pstmt.setTimestamp(index, Timestamp.valueOf((java.time.LocalDateTime) param));
            } else if (param instanceof java.time.LocalDate) {
                pstmt.setDate(index, java.sql.Date.valueOf((java.time.LocalDate) param));
            } else {
                pstmt.setObject(index, param);
            }
        }
    }

    /**
     * Log an error with context information
     */
    protected void logError(String message, String sql, SQLException e) {
        String className = this.getClass().getSimpleName();
        System.err.println(String.format("[%s] %s", className, message));
        if (sql != null && !sql.isEmpty()) {
            System.err.println("SQL: " + sql);
        }
        System.err.println("Error Code: " + e.getErrorCode());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Functional interface for mapping ResultSet to objects
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Functional interface for transaction operations
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws SQLException;
    }
}
