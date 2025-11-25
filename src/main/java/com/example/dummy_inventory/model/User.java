package com.example.dummy_inventory.model;

import java.time.LocalDateTime;

public class User {
    // Enum for user roles
    public enum Role {
        ADMIN, MANAGER, USER
    }

    private int userId;
    private String username;
    private String password;
    private Role role;
    private String fullName;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Default constructor
    public User() {
        this.role = Role.USER;  // Default role
        this.isActive = true;   // Active by default
    }

    // Constructor with all parameters
    public User(int userId, String username, String password, Role role,
                String fullName, String email, boolean isActive,
                LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Constructor for basic authentication (backwards compatible)
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = Role.USER;
        this.isActive = true;
    }

    // Constructor without userId (for creating new users)
    public User(String username, String password, Role role, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.role = role != null ? role : Role.USER;
        this.fullName = fullName;
        this.email = email;
        this.isActive = true;
    }

    // Simple constructor (backwards compatible)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = Role.USER;
        this.isActive = true;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Permission check methods
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManager() {
        return role == Role.MANAGER || role == Role.ADMIN;
    }

    public boolean canManageUsers() {
        return role == Role.ADMIN;
    }

    public boolean canViewReports() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    public boolean canManageInventory() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
