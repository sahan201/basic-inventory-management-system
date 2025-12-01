package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.UserDAO;
import com.example.dummy_inventory.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

                try {
                    if (userDAO.createUser(newUser)) {
                        showSuccess("User created successfully!");
                        clearForm();
                        loadUsers();
                    } else {
                        showError("Failed to create user. Please try again.");
                    }
                } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                    showError("Username '" + usernameField.getText().trim() + "' already exists. Please choose a different username.");
                } catch (java.sql.SQLException e) {
                    // Check for MySQL duplicate entry error code (1062)
                    if (e.getErrorCode() == 1062) {
                        showError("Username '" + usernameField.getText().trim() + "' already exists. Please choose a different username.");
                    } else {
                        showError("Database error: " + e.getMessage());
                    }
                    e.printStackTrace();
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
                    showError("Failed to update user. Username may already exist.");
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
        dialog.setContentText("New password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (!password.isEmpty()) {
                if (userDAO.updatePassword(user.getUserId(), password)) {
                    showSuccess("Password reset successfully!");
                } else {
                    showError("Failed to reset password.");
                }
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

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required.");
            return false;
        }
        if (selectedUser == null && passwordField.getText().isEmpty()) {
            showError("Password is required for new users.");
            return false;
        }
        if (roleComboBox.getValue() == null) {
            showError("Please select a role.");
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
