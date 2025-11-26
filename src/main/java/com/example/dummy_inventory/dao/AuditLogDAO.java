package com.example.dummy_inventory.dao;

import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.AuditLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    /**
     * Create a new audit log entry
     */
    public boolean createAuditLog(AuditLog log) {
        String sql = "INSERT INTO AuditLog (user_id, action, table_name, record_id, " +
                     "old_value, new_value, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (log.getUserId() != null) {
                pstmt.setInt(1, log.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            pstmt.setString(2, log.getAction());
            pstmt.setString(3, log.getTableName());

            if (log.getRecordId() != null) {
                pstmt.setInt(4, log.getRecordId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setString(5, log.getOldValue());
            pstmt.setString(6, log.getNewValue());
            pstmt.setString(7, log.getIpAddress());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating audit log:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all audit logs with username
     */
    public List<AuditLog> getAllAuditLogs() {
        return getAllAuditLogs(1000); // Default limit
    }

    /**
     * Get audit logs with limit
     */
    public List<AuditLog> getAllAuditLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "ORDER BY al.timestamp DESC " +
                     "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting all audit logs:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by user ID
     */
    public List<AuditLog> getAuditLogsByUser(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "WHERE al.user_id = ? " +
                     "ORDER BY al.timestamp DESC " +
                     "LIMIT 1000";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit logs by user:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by action type
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "WHERE al.action = ? " +
                     "ORDER BY al.timestamp DESC " +
                     "LIMIT 1000";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, action);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit logs by action:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by table name
     */
    public List<AuditLog> getAuditLogsByTable(String tableName) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "WHERE al.table_name = ? " +
                     "ORDER BY al.timestamp DESC " +
                     "LIMIT 1000";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit logs by table:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by date range
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "WHERE al.timestamp BETWEEN ? AND ? " +
                     "ORDER BY al.timestamp DESC " +
                     "LIMIT 1000";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit logs by date range:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs for a specific record
     */
    public List<AuditLog> getAuditLogsForRecord(String tableName, int recordId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM AuditLog al " +
                     "LEFT JOIN User u ON al.user_id = u.user_id " +
                     "WHERE al.table_name = ? AND al.record_id = ? " +
                     "ORDER BY al.timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tableName);
            pstmt.setInt(2, recordId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = extractAuditLogFromResultSet(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit logs for record:");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get count of audit logs
     */
    public int getAuditLogCount() {
        String sql = "SELECT COUNT(*) AS count FROM AuditLog";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting audit log count:");
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Extract AuditLog object from ResultSet
     */
    private AuditLog extractAuditLogFromResultSet(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }

        log.setAction(rs.getString("action"));
        log.setTableName(rs.getString("table_name"));

        int recordId = rs.getInt("record_id");
        if (!rs.wasNull()) {
            log.setRecordId(recordId);
        }

        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setIpAddress(rs.getString("ip_address"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            log.setTimestamp(timestamp.toLocalDateTime());
        }

        return log;
    }
}
