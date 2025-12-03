package com.example.dummy_inventory;

import com.example.dummy_inventory.db.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Main Application Entry Point - IMPROVED VERSION
 * Handles proper lifecycle management, error handling, and resource cleanup
 *
 * IMPROVEMENTS:
 * - init() method for pre-flight checks (database connection test)
 * - Proper exception handling with user-friendly alerts
 * - CSS stylesheet loading
 * - Application icon support
 * - Clean shutdown handling
 * - Confirmation dialog on exit
 * - Uncaught exception handler
 */
public class HelloApplication extends Application {

    private static Stage primaryStage;

    /**
     * Initialize method - runs before start()
     * Use for pre-flight checks and resource initialization
     */
    @Override
    public void init() throws Exception {
        super.init();

        System.out.println("Initializing Inventory Management System...");

        // Test database connection before launching UI
        if (!DatabaseConnection.testConnection()) {
            throw new RuntimeException(
                    "Unable to connect to database. " +
                    "Please check your database.properties configuration and ensure MySQL is running."
            );
        }

        System.out.println("✓ Database connection verified");
    }

    /**
     * Start method - launches the UI
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 400, 550);

            // Load CSS stylesheet if it exists
            String cssPath = "/css/styles.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                System.out.println("✓ CSS stylesheet loaded");
            }

            // Configure stage
            stage.setTitle("Inventory Management System - Login");
            stage.setScene(scene);
            stage.setMinWidth(350);
            stage.setMinHeight(450);
            stage.setResizable(true);

            // Set application icon if available
            try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
                if (iconStream != null) {
                    stage.getIcons().add(new Image(iconStream));
                    System.out.println("✓ Application icon loaded");
                }
            } catch (Exception e) {
                // Icon not found, continue without it
                System.out.println("ℹ Application icon not found (optional)");
            }

            // Handle close request with confirmation
            stage.setOnCloseRequest(event -> {
                event.consume(); // Prevent default close
                handleApplicationClose();
            });

            stage.show();
            stage.centerOnScreen();

            System.out.println("✓ Application started successfully");

        } catch (IOException e) {
            System.err.println("✗ Failed to load login view");
            showErrorAlert("Failed to load login view", e);
            Platform.exit();
        }
    }

    /**
     * Stop method - runs when application is closing
     * Use for cleanup operations
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Application stopping...");

        // Cleanup resources here
        // If using HikariCP connection pooling:
        // DatabaseConnectionPooled.shutdown();

        System.out.println("✓ Application stopped");
        super.stop();
    }

    /**
     * Handle application close request
     * Shows confirmation dialog and performs cleanup
     */
    private void handleApplicationClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Exit Application?");
        alert.setContentText("Are you sure you want to exit the Inventory Management System?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Perform cleanup
                try {
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * Show error alert with exception details
     */
    private void showErrorAlert(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());

        // Add expandable exception details
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        textArea.setText(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new javafx.scene.control.Label("Exception details:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Get the primary stage (useful for other controllers)
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navigate to a different scene (helper method for controllers)
     *
     * @param fxmlPath Path to FXML file
     * @param title Window title
     */
    public static void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            String cssPath = "/css/styles.css";
            if (HelloApplication.class.getResource(cssPath) != null) {
                scene.getStylesheets().add(
                        HelloApplication.class.getResource(cssPath).toExternalForm()
                );
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Inventory Management - " + title);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setContentText("Failed to load: " + fxmlPath);
            alert.showAndWait();
        }
    }

    /**
     * Main method - entry point
     */
    public static void main(String[] args) {
        // Set uncaught exception handler for JavaFX thread
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("✗ Uncaught exception in thread: " + thread.getName());
            throwable.printStackTrace();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unexpected Error");
                alert.setHeaderText("An unexpected error occurred");
                alert.setContentText(throwable.getMessage());
                alert.showAndWait();
            });
        });

        System.out.println("========================================");
        System.out.println("Inventory Management System");
        System.out.println("========================================");

        launch(args);
    }
}