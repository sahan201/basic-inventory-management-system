package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.db.DatabaseConnection;
import com.example.dummy_inventory.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Controller for the main Dashboard view
 * Manages navigation and displays statistics
 */
public class DashboardController {

    // Header components
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    // Navigation buttons
    @FXML
    private Button btnProducts;

    @FXML
    private Button btnCategories;

    @FXML
    private Button btnSuppliers;

    @FXML
    private Button btnSales;

    @FXML
    private Button btnDashboard;

    // Content area
    @FXML
    private StackPane contentArea;

    @FXML
    private VBox dashboardPane;

    // Dashboard statistics labels
    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalCategoriesLabel;

    @FXML
    private Label totalSuppliersLabel;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label lowStockLabel;

    // Status bar
    @FXML
    private Label statusLabel;

    // Store current user
    private User currentUser;

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get current user from LoginController
        currentUser = LoginController.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " 👋");
        }

        // Load dashboard statistics
        loadDashboardStats();

        // Set dashboard as active tab
        setActiveTab(btnDashboard);

        // Add hover effects to navigation buttons
        addNavButtonHoverEffects();

        setStatus("Dashboard loaded successfully");
    }

    /**
     * Loads all dashboard statistics from database
     */
    private void loadDashboardStats() {
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Get total products
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Product");
                int totalProducts = rs.next() ? rs.getInt("count") : 0;

                // Get total categories
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Category");
                int totalCategories = rs.next() ? rs.getInt("count") : 0;

                // Get total suppliers
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Supplier");
                int totalSuppliers = rs.next() ? rs.getInt("count") : 0;

                // Get total sales
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Sale");
                int totalSales = rs.next() ? rs.getInt("count") : 0;

                // Get low stock products (quantity < 20)
                rs = stmt.executeQuery(
                        "SELECT name, quantity_in_stock FROM Product " +
                                "WHERE quantity_in_stock < 20 ORDER BY quantity_in_stock ASC LIMIT 5"
                );

                StringBuilder lowStockText = new StringBuilder();
                if (!rs.next()) {
                    lowStockText.append("All products are well stocked! ✓");
                } else {
                    do {
                        lowStockText.append(String.format("• %s (Stock: %d)\n",
                                rs.getString("name"),
                                rs.getInt("quantity_in_stock")
                        ));
                    } while (rs.next());
                }

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    totalProductsLabel.setText(String.valueOf(totalProducts));
                    totalCategoriesLabel.setText(String.valueOf(totalCategories));
                    totalSuppliersLabel.setText(String.valueOf(totalSuppliers));
                    totalSalesLabel.setText(String.valueOf(totalSales));
                    lowStockLabel.setText(lowStockText.toString());
                });

            } catch (SQLException e) {
                System.err.println("Error loading dashboard stats:");
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Error loading statistics"));
            }
        }).start();
    }

    /**
     * Shows the dashboard view
     */
    @FXML
    private void showDashboard() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardPane);
        setActiveTab(btnDashboard);
        loadDashboardStats(); // Refresh stats
        setStatus("Dashboard view");
    }

    /**
     * Shows the products management view
     */
    @FXML
    private void showProducts() {
        loadView("ProductsView.fxml", "Products Management");
        setActiveTab(btnProducts);
        setStatus("Products view");
    }

    /**
     * Shows the categories management view
     */
    @FXML
    private void showCategories() {
        loadView("CategoriesView.fxml", "Categories Management");
        setActiveTab(btnCategories);
        setStatus("Categories view");
    }

    /**
     * Shows the suppliers management view
     */
    @FXML
    private void showSuppliers() {
        loadView("SuppliersView.fxml", "Suppliers Management");
        setActiveTab(btnSuppliers);
        setStatus("Suppliers view");
    }

    /**
     * Shows the sales management view
     */
    @FXML
    private void showSales() {
        loadView("SalesView.fxml", "Sales Management");
        setActiveTab(btnSales);
        setStatus("Sales view");
    }

    /**
     * Loads a view into the content area
     *
     * @param fxmlFile The FXML file name
     * @param viewName The name of the view for error messages
     */
    private void loadView(String fxmlFile, String viewName) {
        try {
            // For now, show a placeholder since we haven't created these views yet
            VBox placeholder = new VBox(20);
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            placeholder.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 50;");

            Label titleLabel = new Label("🚧 " + viewName);
            titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #667eea;");

            Label messageLabel = new Label("This view is under construction.\nIt will be implemented in the next phase.");
            messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666666; -fx-text-alignment: center;");
            messageLabel.setWrapText(true);

            Button backButton = new Button("← Back to Dashboard");
            backButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
            backButton.setOnAction(e -> showDashboard());

            placeholder.getChildren().addAll(titleLabel, messageLabel, backButton);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(placeholder);

            /* Uncomment this when views are created:
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/dummy_inventory/view/" + fxmlFile)
            );
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            */

        } catch (Exception e) {
            System.err.println("Error loading " + viewName + ":");
            e.printStackTrace();
            setStatus("Error loading " + viewName);
        }
    }

    /**
     * Handles logout button click
     */
    @FXML
    private void handleLogout() {
        try {
            // Clear current user
            LoginController.logout();

            // Load login view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/dummy_inventory/view/LoginView.fxml")
            );
            Parent loginRoot = loader.load();

            // Get current stage and switch to login scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Inventory Management - Login");
            stage.centerOnScreen();

            System.out.println("User logged out successfully");

        } catch (IOException e) {
            System.err.println("Error during logout:");
            e.printStackTrace();
            setStatus("Error during logout");
        }
    }

    /**
     * Sets the active navigation tab style
     *
     * @param activeButton The button to mark as active
     */
    private void setActiveTab(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {btnDashboard, btnProducts, btnCategories, btnSuppliers, btnSales};

        for (Button btn : navButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                    "-fx-padding: 12 25; -fx-cursor: hand; " +
                    "-fx-border-width: 0 0 3 0; -fx-border-color: transparent;");
        }

        // Highlight active button
        activeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-padding: 12 25; -fx-cursor: hand; " +
                "-fx-border-width: 0 0 3 0; -fx-border-color: #ffffff;");
    }

    /**
     * Adds hover effects to navigation buttons
     */
    private void addNavButtonHoverEffects() {
        Button[] navButtons = {btnDashboard, btnProducts, btnCategories, btnSuppliers, btnSales};

        for (Button btn : navButtons) {
            btn.setOnMouseEntered(e -> {
                if (!btn.getStyle().contains("#ffffff")) { // Not active
                    btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(255,255,255,0.1);");
                }
            });

            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("border-color: #ffffff")) { // Not active
                    btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.1);", ""));
                }
            });
        }
    }

    /**
     * Sets the status bar message
     *
     * @param message The status message to display
     */
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Refreshes the current view
     */
    public void refreshView() {
        if (contentArea.getChildren().contains(dashboardPane)) {
            loadDashboardStats();
        }
        // Add refresh logic for other views when implemented
    }
}

/*
============================================
DASHBOARD CONTROLLER FEATURES:
============================================

1. INITIALIZATION:
   ✅ Gets current user from LoginController
   ✅ Displays welcome message
   ✅ Loads dashboard statistics
   ✅ Sets up navigation

2. STATISTICS LOADING:
   ✅ Total products count
   ✅ Total categories count
   ✅ Total suppliers count
   ✅ Total sales count
   ✅ Low stock alerts (products < 20)
   ✅ Background thread (non-blocking)

3. NAVIGATION:
   ✅ Dashboard view (default)
   ✅ Products management
   ✅ Categories management
   ✅ Suppliers management
   ✅ Sales management
   ✅ Active tab highlighting

4. USER INTERACTIONS:
   ✅ Logout functionality
   ✅ Navigation button hover effects
   ✅ Quick action buttons
   ✅ Status bar updates

5. VIEW MANAGEMENT:
   ✅ Dynamic content loading
   ✅ Placeholder views (until CRUD views are created)
   ✅ Back to dashboard navigation

============================================
NEXT STEPS:
============================================

To complete the application, create these CRUD views:

1. ProductsView.fxml + ProductsController.java
   - TableView with products
   - Add/Edit/Delete buttons
   - Search functionality

2. CategoriesView.fxml + CategoriesController.java
   - Categories CRUD operations

3. SuppliersView.fxml + SuppliersController.java
   - Suppliers CRUD operations

4. SalesView.fxml + SalesController.java
   - Record sales
   - View sales history

5. DAO Classes:
   - ProductDAO.java
   - CategoryDAO.java
   - SupplierDAO.java
   - SaleDAO.java

============================================
USAGE:
============================================

The dashboard automatically:
- Shows current user's name
- Loads real-time statistics
- Provides navigation to all modules
- Shows low stock alerts
- Allows logout

Navigation buttons will show placeholders until
the respective views are created.

============================================
*/