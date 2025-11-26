package com.example.dummy_inventory.model;

import java.time.LocalDateTime;

public class AuditLog {
    private int logId;
    private Integer userId;
    private String action;
    private String tableName;
    private Integer recordId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime timestamp;

    // Additional field for display
    private String username;

    // Default constructor
    public AuditLog() {
    }

    // Constructor with all fields
    public AuditLog(int logId, Integer userId, String action, String tableName,
                    Integer recordId, String oldValue, String newValue,
                    String ipAddress, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Constructor for creating new logs
    public AuditLog(Integer userId, String action, String tableName,
                    Integer recordId, String oldValue, String newValue, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", tableName='" + tableName + '\'' +
                ", recordId=" + recordId +
                ", timestamp=" + timestamp +
                '}';
    }
}
