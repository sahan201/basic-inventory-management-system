package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.CategoryDAO;
import com.example.dummy_inventory.model.Category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Categories CRUD operations
 */
public class CategoriesController {

    private CategoryDAO categoryDAO = new CategoryDAO();

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;
    @FXML private Button saveButton;

    private ObservableList<Category> categoryList;
    private Category selectedCategory = null;
    private boolean isEditMode = false;

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        setupTableSelection();
    }

    /**
     * Setup table columns with property bindings
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    /**
     * Setup table row selection listener
     */
    private void setupTableSelection() {
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedCategory = newValue;
                    }
                }
        );

        // Double-click to edit
        categoryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedCategory != null) {
                handleEdit();
            }
        });
    }

    /**
     * Load all categories from database
     */
    private void loadCategories() {
        List<Category> categories = categoryDAO.getAllCategories();
        categoryList = FXCollections.observableArrayList(categories);
        categoryTable.setItems(categoryList);
        updateTotalLabel();
        setStatus("Categories loaded", true);
    }

    /**
     * Handle New button - clear form for new entry
     */
    @FXML
    private void handleNew() {
        clearForm();
        isEditMode = false;
        saveButton.setText("💾 Save New");
        setStatus("Enter details for new category", true);
    }

    /**
     * Handle Edit button - load selected category into form
     */
    @FXML
    private void handleEdit() {
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to edit.");
            return;
        }

        isEditMode = true;
        saveButton.setText("💾 Update");

        nameField.setText(selectedCategory.getName());
        descriptionField.setText(selectedCategory.getDescription());

        setStatus("Editing: " + selectedCategory.getName(), true);
    }

    /**
     * Handle Save button - add or update category
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (isEditMode && selectedCategory != null) {
            // UPDATE existing category
            selectedCategory.setName(name);
            selectedCategory.setDescription(description);

            if (categoryDAO.updateCategory(selectedCategory)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
                setStatus("Category updated: " + name, true);
                clearForm();
                loadCategories();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category.");
                setStatus("Failed to update category", false);
            }
        } else {
            // CREATE new category
            Category newCategory = new Category(name, description);

            if (categoryDAO.createCategory(newCategory)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
                setStatus("Category added: " + name, true);
                clearForm();
                loadCategories();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category.");
                setStatus("Failed to add category", false);
            }
        }
    }

    /**
     * Handle Delete button - remove selected category
     */
    @FXML
    private void handleDelete() {
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Category");
        confirm.setContentText("Are you sure you want to delete: " + selectedCategory.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (categoryDAO.deleteCategory(selectedCategory.getCategoryId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
                setStatus("Category deleted", true);
                clearForm();
                loadCategories();
                selectedCategory = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete. Category may be in use by products.");
                setStatus("Failed to delete", false);
            }
        }
    }

    /**
     * Handle Clear button - reset form
     */
    @FXML
    private void handleClear() {
        clearForm();
        setStatus("Form cleared", true);
    }

    /**
     * Handle Search button
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadCategories();
            return;
        }

        List<Category> results = categoryDAO.searchCategories(searchTerm);
        categoryList = FXCollections.observableArrayList(results);
        categoryTable.setItems(categoryList);
        updateTotalLabel();
        setStatus("Found " + results.size() + " category(ies)", true);
    }

    /**
     * Clear all form fields
     */
    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        isEditMode = false;
        selectedCategory = null;
        saveButton.setText("💾 Save New");
        statusLabel.setText("");
    }

    /**
     * Validate form input
     */
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name is required!");
            setStatus("Please enter category name", false);
            return false;
        }
        return true;
    }

    /**
     * Update total categories label
     */
    private void updateTotalLabel() {
        totalLabel.setText("Total: " + categoryList.size());
    }

    /**
     * Set status message
     */
    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setTextFill(success ? Color.GREEN : Color.RED);
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

/*
============================================
CATEGORIES CONTROLLER - COMPLETE
============================================

✅ FEATURES:
- Full CRUD operations (Create, Read, Update, Delete)
- Search functionality
- Form validation
- Double-click to edit
- Confirmation dialogs
- Status messages with color coding
- Error handling

✅ USAGE:
This controller works with CategoriesView.fxml

Copy to:
com/example/dummy_inventory/controller/CategoriesController.java

Make sure:
- CategoryDAO is in 'dao' package
- Category model is in 'model' package
- CategoriesView.fxml is in resources folder

============================================
*/