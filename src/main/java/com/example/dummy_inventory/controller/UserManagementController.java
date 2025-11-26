package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.UserDAO;
import com.example.dummy_inventory.model.User;
import com.example.dummy_inventory.util.PasswordValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, LocalDateTime> lastLoginColumn;

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<User.Role> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private UserDAO userDAO;
    private ObservableList<User> usersList;
    private User selectedUser;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        usersList = FXCollections.observableArrayList();

        // Check permissions
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.canManageUsers()) {
            showError("Access Denied: Admin privileges required");
            saveButton.setDisable(true);
            return;
        }

        // Setup table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        // Setup role ComboBox
        roleComboBox.setItems(FXCollections.observableArrayList(User.Role.values()));
        roleComboBox.setValue(User.Role.USER);

        // Active by default
        activeCheckBox.setSelected(true);

        // Load users
        loadUsers();

        // Double-click to edit
        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEdit();
            }
        });
    }

    private void loadUsers() {
        usersList.clear();
        List<User> users = userDAO.getAllUsers();
        usersList.addAll(users);
        usersTable.setItems(usersList);
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUsers();
        } else {
            usersList.clear();
            List<User> users = userDAO.searchUsers(searchTerm);
            usersList.addAll(users);
            usersTable.setItems(usersList);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            if (selectedUser == null) {
                // Create new user
                User newUser = new User();
                newUser.setUsername(usernameField.getText().trim());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleComboBox.getValue());
                newUser.setFullName(fullNameField.getText().trim());
                newUser.setEmail(emailField.getText().trim());
                newUser.setActive(activeCheckBox.isSelected());

                if (userDAO.createUser(newUser)) {
                    showSuccess("User created successfully!");
                    clearForm();
                    loadUsers();
                } else {
                    showError("Failed to create user. Username may already exist.");
                }
            } else {
                // Update existing user
                selectedUser.setUsername(usernameField.getText().trim());
                selectedUser.setRole(roleComboBox.getValue());
                selectedUser.setFullName(fullNameField.getText().trim());
                selectedUser.setEmail(emailField.getText().trim());
                selectedUser.setActive(activeCheckBox.isSelected());

                // Update password if provided
                String newPassword = passwordField.getText();
                if (!newPassword.isEmpty()) {
                    userDAO.updatePassword(selectedUser.getUserId(), newPassword);
                }

                if (userDAO.updateUser(selectedUser)) {
                    showSuccess("User updated successfully!");
                    clearForm();
                    loadUsers();
                } else {
                    showError("Failed to update user.");
                }
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            usernameField.setText(selectedUser.getUsername());
            fullNameField.setText(selectedUser.getFullName());
            emailField.setText(selectedUser.getEmail());
            roleComboBox.setValue(selectedUser.getRole());
            activeCheckBox.setSelected(selectedUser.isActive());
            passwordField.clear(); // Don't show password
            saveButton.setText("Update User");
            showInfo("Editing user: " + selectedUser.getUsername());
        } else {
            showError("Please select a user to edit.");
        }
    }

    @FXML
    private void handleDelete() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            showError("Please select a user to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete user: " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(user.getUserId())) {
                showSuccess("User deleted successfully!");
                loadUsers();
            } else {
                showError("Failed to delete user.");
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            showError("Please select a user.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + user.getUsername());
        dialog.setContentText("New password (min 8 chars, 1 uppercase, 1 number):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (!password.isEmpty()) {
                // Validate password strength
                PasswordValidator.ValidationResult validationResult = PasswordValidator.validatePassword(password);
                if (!validationResult.isValid()) {
                    showError("Password validation failed:\n" + validationResult.getErrorMessage());
                    return;
                }

                if (userDAO.updatePassword(user.getUserId(), password)) {
                    showSuccess("Password reset successfully!");
                } else {
                    showError("Failed to reset password.");
                }
            } else {
                showError("Password cannot be empty.");
            }
        });
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(User.Role.USER);
        activeCheckBox.setSelected(true);
        selectedUser = null;
        saveButton.setText("Create User");
        statusLabel.setText("");
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Validate username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errors.append("• Username is required\n");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.append("• Username must be 3-20 characters (letters, numbers, underscore only)\n");
        }

        // Validate password (for new users or when changing password)
        String password = passwordField.getText();
        if (selectedUser == null) {
            // New user - password is required
            if (password.isEmpty()) {
                errors.append("• Password is required for new users\n");
            } else {
                // Validate password strength
                PasswordValidator.ValidationResult passwordResult = PasswordValidator.validatePassword(password);
                if (!passwordResult.isValid()) {
                    for (String error : passwordResult.getErrors()) {
                        errors.append("• ").append(error).append("\n");
                    }
                }
            }
        } else {
            // Editing existing user - only validate if password is provided
            if (!password.isEmpty()) {
                PasswordValidator.ValidationResult passwordResult = PasswordValidator.validatePassword(password);
                if (!passwordResult.isValid()) {
                    for (String error : passwordResult.getErrors()) {
                        errors.append("• ").append(error).append("\n");
                    }
                }
            }
        }

        // Validate full name (optional but should not be too long)
        String fullName = fullNameField.getText().trim();
        if (!fullName.isEmpty() && fullName.length() > 100) {
            errors.append("• Full name must not exceed 100 characters\n");
        }

        // Validate email (if provided)
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.append("• Invalid email format\n");
        }

        // Validate role
        if (roleComboBox.getValue() == null) {
            errors.append("• Please select a role\n");
        }

        // Show errors if any
        if (errors.length() > 0) {
            showError("Validation Errors:\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
    }

    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.BLUE);
    }
}
