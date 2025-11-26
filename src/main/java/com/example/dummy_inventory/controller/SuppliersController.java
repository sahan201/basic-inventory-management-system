package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.SupplierDAO;
import com.example.dummy_inventory.model.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;

public class SuppliersController {

    private SupplierDAO supplierDAO = new SupplierDAO();

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colPhone;

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;
    @FXML private Button saveButton;

    private ObservableList<Supplier> supplierList;
    private Supplier selectedSupplier = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSuppliers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void setupTableSelection() {
        supplierTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedSupplier = newValue;
                    }
                }
        );

        supplierTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedSupplier != null) {
                handleEdit();
            }
        });
    }

    private void loadSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        supplierList = FXCollections.observableArrayList(suppliers);
        supplierTable.setItems(supplierList);
        updateTotalLabel();
        setStatus("Suppliers loaded", true);
    }

    @FXML
    private void handleNew() {
        clearForm();
        isEditMode = false;
        saveButton.setText("💾 Save New");
        setStatus("Enter details for new supplier", true);
    }

    @FXML
    private void handleEdit() {
        if (selectedSupplier == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a supplier to edit.");
            return;
        }

        isEditMode = true;
        saveButton.setText("💾 Update");

        nameField.setText(selectedSupplier.getName());
        contactField.setText(selectedSupplier.getContactPerson());
        emailField.setText(selectedSupplier.getEmail());
        phoneField.setText(selectedSupplier.getPhone());

        setStatus("Editing: " + selectedSupplier.getName(), true);
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (isEditMode && selectedSupplier != null) {
            selectedSupplier.setName(name);
            selectedSupplier.setContactPerson(contact);
            selectedSupplier.setEmail(email);
            selectedSupplier.setPhone(phone);

            if (supplierDAO.updateSupplier(selectedSupplier)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier updated successfully!");
                setStatus("Supplier updated: " + name, true);
                clearForm();
                loadSuppliers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update supplier.");
                setStatus("Failed to update supplier", false);
            }
        } else {
            Supplier newSupplier = new Supplier(name, contact, email, phone);

            if (supplierDAO.createSupplier(newSupplier)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier added successfully!");
                setStatus("Supplier added: " + name, true);
                clearForm();
                loadSuppliers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add supplier.");
                setStatus("Failed to add supplier", false);
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedSupplier == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a supplier to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Supplier");
        confirm.setContentText("Delete: " + selectedSupplier.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (supplierDAO.deleteSupplier(selectedSupplier.getSupplierId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier deleted!");
                setStatus("Supplier deleted", true);
                clearForm();
                loadSuppliers();
                selectedSupplier = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete. Supplier may be in use.");
                setStatus("Failed to delete", false);
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        setStatus("Form cleared", true);
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadSuppliers();
            return;
        }

        List<Supplier> results = supplierDAO.searchSuppliers(searchTerm);
        supplierList = FXCollections.observableArrayList(results);
        supplierTable.setItems(supplierList);
        updateTotalLabel();
        setStatus("Found " + results.size() + " supplier(s)", true);
    }

    private void clearForm() {
        nameField.clear();
        contactField.clear();
        emailField.clear();
        phoneField.clear();
        isEditMode = false;
        selectedSupplier = null;
        saveButton.setText("💾 Save New");
        statusLabel.setText("");
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("• Supplier name is required\n");
        }

        if (contactField.getText().trim().isEmpty()) {
            errors.append("• Contact person is required\n");
        }

        String email = emailField.getText().trim();
        // Improved email validation regex that checks for:
        // - Valid username part (alphanumeric, dots, hyphens, underscores)
        // - @ symbol
        // - Valid domain name
        // - Valid TLD (at least 2 characters)
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("• Invalid email format\n");
        }

        String phone = phoneField.getText().trim();
        // Phone validation: allow digits, spaces, hyphens, parentheses, and plus sign
        // Examples: +1-123-456-7890, (123) 456-7890, 123-456-7890, 1234567890
        if (!phone.isEmpty() && !phone.matches("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,5}[-\\s.]?[0-9]{1,6}$")) {
            errors.append("• Invalid phone number format\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            setStatus("Please fix validation errors", false);
            return false;
        }

        return true;
    }

    private void updateTotalLabel() {
        totalLabel.setText("Total: " + supplierList.size());
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setTextFill(success ? Color.GREEN : Color.RED);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}