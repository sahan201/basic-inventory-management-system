package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.UserDAO;
import com.example.dummy_inventory.model.User;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

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
     * FIXED: Uses Task and PauseTransition instead of Thread.sleep() to avoid blocking
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

        // Disable form during registration
        setFormDisabled(true);
        showInfo("Creating account...");

        // Use Task for database operation (FIXED from raw Thread)
        Task<Boolean> registerTask = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                // Check if username exists
                if (userDAO.getUserByUsername(username) != null) {
                    throw new SQLException("Username already exists");
                }

                // Create new user with USER role (self-registered users get basic role)
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setFullName(fullName);
                newUser.setEmail(email);
                newUser.setRole(User.Role.USER); // Default role for self-registration
                newUser.setActive(true); // Active by default

                return userDAO.createUser(newUser);
            }
        };

        registerTask.setOnSucceeded(event -> {
            setFormDisabled(false);
            if (registerTask.getValue()) {
                showSuccess("Account created successfully! Redirecting to login...");

                // FIXED: Use PauseTransition instead of Thread.sleep - proper JavaFX way
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> handleBackToLogin());
                pause.play();
            } else {
                showError("Failed to create account. Please try again.");
            }
        });

        registerTask.setOnFailed(event -> {
            setFormDisabled(false);
            Throwable ex = registerTask.getException();
            if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
                showError("Username '" + username + "' already exists. Please choose a different username.");
            } else if (ex instanceof SQLException && ((SQLException)ex).getErrorCode() == 1062) {
                showError("Username '" + username + "' already exists. Please choose a different username.");
            } else {
                showError("Database error: " + ex.getMessage());
            }
        });

        // Execute on background thread
        Thread thread = new Thread(registerTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Enable or disable all form fields
     */
    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        fullNameField.setDisable(disabled);
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        confirmPasswordField.setDisable(disabled);
        registerButton.setDisable(disabled);
    }

    /**
     * Display info message
     */
    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.BLUE);
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
