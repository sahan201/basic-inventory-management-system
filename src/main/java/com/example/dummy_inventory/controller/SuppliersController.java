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

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
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

        if (isEditMode && selectedSupplier != null) {
            selectedSupplier.setName(name);
            selectedSupplier.setContactPerson(contact);
            selectedSupplier.setEmail(email);

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
            Supplier newSupplier = new Supplier(name, contact, email);

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
        isEditMode = false;
        selectedSupplier = null;
        saveButton.setText("💾 Save New");
        statusLabel.setText("");
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Validate supplier name
        String supplierName = nameField.getText().trim();
        if (supplierName.isEmpty()) {
            errors.append("• Supplier name is required\n");
        } else if (supplierName.length() < 2) {
            errors.append("• Supplier name must be at least 2 characters\n");
        } else if (supplierName.length() > 200) {
            errors.append("• Supplier name must not exceed 200 characters\n");
        }

        // Validate contact person
        String contactPerson = contactField.getText().trim();
        if (contactPerson.isEmpty()) {
            errors.append("• Contact person is required\n");
        } else if (contactPerson.length() < 2) {
            errors.append("• Contact person must be at least 2 characters\n");
        } else if (contactPerson.length() > 100) {
            errors.append("• Contact person must not exceed 100 characters\n");
        }

        // Validate email (improved regex)
        String email = emailField.getText().trim();
        if (!email.isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.append("• Invalid email format (example: supplier@company.com)\n");
            } else if (email.length() > 100) {
                errors.append("• Email must not exceed 100 characters\n");
            }
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