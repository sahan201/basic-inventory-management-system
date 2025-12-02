package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.UserDAO;
import com.example.dummy_inventory.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for user registration
 * Handles new user account creation with validation
 */
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Hyperlink loginLink;

    private UserDAO userDAO;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        statusLabel.setText("");
    }

    /**
     * Handle registration button click
     */
    @FXML
    private void handleRegister() {
        // Clear previous status
        statusLabel.setText("");

        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get form data
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            showError("Username already exists. Please choose a different username.");
            return;
        }

        // Create new user with USER role (self-registered users get basic role)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setEmail(email);

        // IMPORTANT: Self-registered users are assigned the USER role by default
        // This means they will have LIMITED ACCESS and may see "Access Denied" errors
        // when attempting to access features like:
        // - User Management (ADMIN only)
        // - Reports (MANAGER/ADMIN only)
        // - Product Management (MANAGER/ADMIN only)
        // An existing ADMIN user must upgrade their role via User Management for full access.
        newUser.setRole(User.Role.USER); // Default role for self-registration
        newUser.setActive(true); // Active by default

        // Attempt to create user
        // Attempt to create user
        try {
            if (userDAO.createUser(newUser)) {
                showSuccess("Account created successfully! Redirecting to login...");

                // Wait 2 seconds then navigate to login
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(this::handleBackToLogin);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                showError("Failed to create account. Please try again.");
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            showError("Username '" + username + "' already exists. Please choose a different username.");
        } catch (java.sql.SQLException e) {
            // Check for MySQL duplicate entry error code (1062)
            if (e.getErrorCode() == 1062) {
                showError("Username '" + username + "' already exists. Please choose a different username.");
            } else {
                showError("Database error: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    /**
     * Navigate back to login page
     */
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/LoginView.fxml")
            );
            Parent loginRoot = loader.load();

            // Get current stage and switch scene
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Inventory Management - Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading login view:");
            e.printStackTrace();
            showError("Error navigating to login page");
        }
    }

    /**
     * Validate all input fields
     *
     * @return true if all validation passes
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Username validation
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errors.append("• Username is required\n");
        } else if (username.length() < 3) {
            errors.append("• Username must be at least 3 characters\n");
        } else if (username.length() > 50) {
            errors.append("• Username must not exceed 50 characters\n");
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            errors.append("• Username can only contain letters, numbers, and underscores\n");
        }

        // Full name validation
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            errors.append("• Full name is required\n");
        } else if (fullName.length() < 2) {
            errors.append("• Full name must be at least 2 characters\n");
        } else if (fullName.length() > 100) {
            errors.append("• Full name must not exceed 100 characters\n");
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errors.append("• Email is required\n");
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("• Invalid email format\n");
        }

        // Password validation
        String password = passwordField.getText();
        if (password.isEmpty()) {
            errors.append("• Password is required\n");
        } else if (password.length() < 6) {
            errors.append("• Password must be at least 6 characters\n");
        } else if (password.length() > 100) {
            errors.append("• Password must not exceed 100 characters\n");
        }

        // Confirm password validation
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            errors.append("• Please confirm your password\n");
        } else if (!password.equals(confirmPassword)) {
            errors.append("• Passwords do not match\n");
        }

        // Show errors if any
        if (errors.length() > 0) {
            showError(errors.toString().trim());
            return false;
        }

        return true;
    }

    /**
     * Display success message
     *
     * @param message Success message to display
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
    }

    /**
     * Display error message
     *
     * @param message Error message to display
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
    }
}

/*
============================================
REGISTER CONTROLLER FEATURES
============================================

✅ COMPREHENSIVE VALIDATION:
   - Username: 3-50 chars, alphanumeric + underscores only
   - Full Name: 2-100 chars, required
   - Email: Valid email format with proper regex
   - Password: 6-100 chars minimum
   - Password Match: Confirms passwords match
   - Duplicate Check: Prevents duplicate usernames

✅ SECURITY:
   - Passwords hashed by UserDAO using BCrypt
   - Self-registered users get USER role by default
   - Cannot self-register as ADMIN or MANAGER
   - Active by default for immediate use

✅ USER EXPERIENCE:
   - Clear validation messages
   - Success message before redirect
   - 2-second delay to read success message
   - Easy navigation back to login
   - Enter key on confirm password triggers registration

✅ ERROR HANDLING:
   - Comprehensive input validation
   - Database error handling
   - Navigation error handling
   - User-friendly error messages

============================================
DEFAULT ROLE ASSIGNMENT
============================================

Self-registered users receive:
- Role: USER (basic access)
- Active: true (can login immediately)
- Cannot access admin features

Admins can later upgrade roles via User Management

============================================
*/
