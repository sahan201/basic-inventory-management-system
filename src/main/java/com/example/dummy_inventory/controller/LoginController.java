package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.UserDAO;
import com.example.dummy_inventory.model.User;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the Login View
 * Handles user authentication and navigation to main dashboard
 */
public class LoginController {

    // FXML linked components
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private javafx.scene.control.Hyperlink registerLink;

    // Store logged-in user info
    private static User currentUser;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Clear status label on start
        statusLabel.setText("");

        // Add Enter key listener to username field
        usernameField.setOnAction(event -> passwordField.requestFocus());

        // Optional: Add visual feedback on button hover
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;")
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;")
        );
    }

    /**
     * Handles login button click and Enter key on password field
     * FIXED: Uses Task and PauseTransition instead of Thread.sleep() to avoid blocking UI
     */
    @FXML
    protected void handleLogin() {
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            setStatusMessage("⚠ Username and password are required.", Color.web("#e74c3c"));
            shakeLoginButton();
            return;
        }

        // Disable button during login attempt
        loginButton.setDisable(true);
        setStatusMessage("Logging in...", Color.web("#3498db"));

        // Use Task for proper background execution (FIXED from Thread)
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return validateLogin(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            loginButton.setDisable(false);

            if (user != null) {
                currentUser = user;
                setStatusMessage("✓ Login Successful!", Color.web("#27ae60"));

                // FIXED: Use PauseTransition instead of Thread.sleep - NON-BLOCKING!
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> openDashboard());
                pause.play();
            } else {
                setStatusMessage("✗ Invalid username or password.", Color.web("#e74c3c"));
                shakeLoginButton();

                // Clear password field for security
                passwordField.clear();
            }
        });

        loginTask.setOnFailed(event -> {
            loginButton.setDisable(false);
            Throwable exception = loginTask.getException();
            setStatusMessage("⚠ Login error: " + exception.getMessage(), Color.web("#e74c3c"));
            exception.printStackTrace();
        });

        // Execute on background thread
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * Validates login credentials against the database using BCrypt password hashing
     *
     * @param username The username to check
     * @param password The password to check
     * @return User object if valid, null otherwise
     */
    private User validateLogin(String username, String password) {
        UserDAO userDAO = new UserDAO();

        try {
            // Use UserDAO.login() which handles BCrypt verification
            return userDAO.login(username, password);
        } catch (Exception e) {
            System.err.println("Error during login validation:");
            e.printStackTrace();

            Platform.runLater(() ->
                    setStatusMessage("⚠ Database connection error.", Color.web("#e74c3c"))
            );
            return null;
        }
    }

    /**
     * Opens the main dashboard after successful login
     */
    private void openDashboard() {
        try {
            // Load the dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            // C:\javaInventoryAssign\dummy_inventory\src\main\resources\fxml\CategoriesView.fxml

            // Optional: Pass current user to dashboard controller
            // DashboardController dashboardController = loader.getController();
            // dashboardController.setCurrentUser(currentUser);

            // Create new scene
            Scene dashboardScene = new Scene(dashboardRoot);

            // Get current stage and switch scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Inventory Management - Dashboard");
            stage.centerOnScreen();

            System.out.println("User logged in: " + currentUser.getUsername());

        } catch (IOException e) {
            System.err.println("Error loading dashboard:");
            e.printStackTrace();
            setStatusMessage("⚠ Error loading dashboard.", Color.web("#e74c3c"));
        }
    }

    /**
     * Adds a shake animation to the login button for visual feedback
     * FIXED: Uses JavaFX TranslateTransition instead of Thread.sleep()
     */
    private void shakeLoginButton() {
        // Use JavaFX animation instead of Thread manipulation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginButton);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setByX(5);
        shake.play();
    }

    /**
     * Helper method to set status message with color
     *
     * @param message The message to display
     * @param color The color of the text
     */
    private void setStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }

    /**
     * Navigate to registration page
     */
    @FXML
    protected void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RegisterView.fxml")
            );
            Parent registerRoot = loader.load();

            // Get current stage and switch scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene registerScene = new Scene(registerRoot);
            stage.setScene(registerScene);
            stage.setTitle("Inventory Management - Register");
            stage.centerOnScreen();

            System.out.println("Navigated to registration page");

        } catch (IOException e) {
            System.err.println("Error loading registration view:");
            e.printStackTrace();
            setStatusMessage("⚠ Error loading registration page.", Color.web("#e74c3c"));
        }
    }

    /**
     * Optional: Handle "Forgot Password" action
     */
    @FXML
    protected void handleForgotPassword() {
        setStatusMessage("Contact administrator for password reset.", Color.web("#3498db"));
    }

    /**
     * Get the currently logged-in user
     *
     * @return Current User object
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logout - clear current user
     */
    public static void logout() {
        currentUser = null;
    }
}

/*
============================================
IMPROVEMENTS MADE:
============================================

1. ENHANCED FUNCTIONALITY:
   ✅ Background thread for login (non-blocking UI)
   ✅ Dashboard navigation after successful login
   ✅ Current user storage (static field)
   ✅ Initialize method for setup
   ✅ Input trimming and validation

2. BETTER UX:
   ✅ Shake animation on failed login
   ✅ Button hover effects
   ✅ Enter key handling (username → password → login)
   ✅ Password field cleared on failed attempt
   ✅ Loading state during login
   ✅ Visual feedback with icons (✓, ✗, ⚠)

3. SECURITY:
   ✅ PreparedStatement (prevents SQL injection)
   ✅ Password cleared on failure
   ✅ TODO comment for password hashing
   ✅ User object returned (not just boolean)

4. CODE QUALITY:
   ✅ Proper JavaDoc comments
   ✅ Error handling with user-friendly messages
   ✅ Separation of concerns
   ✅ Static methods for user management
   ✅ Thread-safe UI updates with Platform.runLater()

5. PRODUCTION READY:
   ✅ Logout method
   ✅ getCurrentUser() for other controllers
   ✅ Proper exception handling
   ✅ Console logging for debugging

============================================
TODO BEFORE PRODUCTION:
============================================

1. PASSWORD HASHING:
   Use BCrypt or similar:

   import org.mindrot.jbcrypt.BCrypt;

   // When creating user:
   String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

   // When validating:
   if (BCrypt.checkpw(plainPassword, hashedPasswordFromDB)) {
       // Valid
   }

2. SESSION MANAGEMENT:
   - Add session timeout
   - Track last activity
   - Auto-logout after inactivity

3. REMEMBER ME:
   - Store encrypted credentials
   - Use secure token storage

4. RATE LIMITING:
   - Limit login attempts
   - Lock account after X failures
   - CAPTCHA for security

============================================
USAGE IN MAIN APPLICATION:
============================================

// In your Main.java or Application class:
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/LoginView.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Inventory Management - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

============================================
GETTING CURRENT USER IN OTHER CONTROLLERS:
============================================

public class DashboardController {
    @FXML
    public void initialize() {
        User currentUser = LoginController.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    @FXML
    private void handleLogout() {
        LoginController.logout();
        // Navigate back to login screen
    }
}

============================================
*/