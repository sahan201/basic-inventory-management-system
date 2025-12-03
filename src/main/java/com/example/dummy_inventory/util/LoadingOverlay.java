package com.example.dummy_inventory.util;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Reusable loading overlay component for JavaFX applications
 *
 * USAGE EXAMPLES:
 *
 * 1. Basic usage with default message:
 *    LoadingOverlay overlay = new LoadingOverlay();
 *    overlay.show();
 *    // ... perform operation ...
 *    overlay.hide();
 *
 * 2. With custom message:
 *    LoadingOverlay overlay = new LoadingOverlay("Loading products...");
 *    overlay.show();
 *
 * 3. Bind to a Task for automatic show/hide:
 *    Task<List<Product>> loadTask = new Task<>() { ... };
 *    overlay.bindToTask(loadTask);
 *    new Thread(loadTask).start();
 *
 * 4. Wrap existing content:
 *    VBox myContent = new VBox();
 *    StackPane wrapper = LoadingOverlay.wrapWithLoading(myContent);
 *    // Now you can show loading by getting the overlay:
 *    LoadingOverlay overlay = (LoadingOverlay) wrapper.getChildren().get(1);
 *    overlay.show();
 *
 * BENEFITS:
 * - Prevents user interaction during loading
 * - Smooth fade animations
 * - Automatic progress tracking with Task binding
 * - Reusable across all views
 */
public class LoadingOverlay extends StackPane {

    private final ProgressIndicator progressIndicator;
    private final Label messageLabel;
    private final VBox content;

    /**
     * Create loading overlay with default message
     */
    public LoadingOverlay() {
        this("Loading...");
    }

    /**
     * Create loading overlay with custom message
     *
     * @param message Message to display while loading
     */
    public LoadingOverlay(String message) {
        // Semi-transparent white background
        setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");
        setAlignment(Pos.CENTER);
        setVisible(false);
        setMouseTransparent(true);

        // Progress spinner
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(60, 60);
        progressIndicator.setStyle("-fx-progress-color: #667eea;");

        // Message label
        messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-weight: bold;");

        // Content container
        content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 30; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        content.getChildren().addAll(progressIndicator, messageLabel);

        getChildren().add(content);
    }

    /**
     * Show the loading overlay with fade-in animation
     */
    public void show() {
        setMouseTransparent(false);
        setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Show with custom message
     *
     * @param message Message to display
     */
    public void show(String message) {
        messageLabel.setText(message);
        show();
    }

    /**
     * Hide the loading overlay with fade-out animation
     */
    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            setMouseTransparent(true);
        });
        fadeOut.play();
    }

    /**
     * Update the displayed message
     *
     * @param message New message to display
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Set progress value
     *
     * @param progress Progress value (0.0 to 1.0, or -1 for indeterminate)
     */
    public void setProgress(double progress) {
        progressIndicator.setProgress(progress);
    }

    /**
     * Bind to a Task for automatic show/hide and progress updates
     * The overlay will:
     * - Show when task starts
     * - Update message from task.messageProperty()
     * - Update progress from task.progressProperty()
     * - Hide when task completes (succeeded, failed, or cancelled)
     *
     * @param task The Task to bind to
     */
    public void bindToTask(Task<?> task) {
        // Bind message and progress
        messageLabel.textProperty().bind(task.messageProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());

        // Show/hide based on task state
        task.setOnRunning(e -> show());
        task.setOnSucceeded(e -> {
            unbindFromTask(task);
            hide();
        });
        task.setOnFailed(e -> {
            unbindFromTask(task);
            hide();
        });
        task.setOnCancelled(e -> {
            unbindFromTask(task);
            hide();
        });
    }

    /**
     * Unbind from a Task
     *
     * @param task The Task to unbind from
     */
    private void unbindFromTask(Task<?> task) {
        messageLabel.textProperty().unbind();
        progressIndicator.progressProperty().unbind();
    }

    /**
     * Convenience method to wrap content with a loading overlay
     * Returns a StackPane with:
     * - Index 0: Your content
     * - Index 1: LoadingOverlay (initially hidden)
     *
     * Usage:
     * <pre>
     * VBox myContent = new VBox();
     * StackPane wrapper = LoadingOverlay.wrapWithLoading(myContent);
     *
     * // To show loading:
     * LoadingOverlay overlay = (LoadingOverlay) wrapper.getChildren().get(1);
     * overlay.show();
     * </pre>
     *
     * @param content The content to wrap
     * @return StackPane containing content and overlay
     */
    public static StackPane wrapWithLoading(Node content) {
        LoadingOverlay overlay = new LoadingOverlay();
        StackPane wrapper = new StackPane(content, overlay);
        return wrapper;
    }

    /**
     * Create a loading overlay that fills its parent
     *
     * @param message Loading message
     * @return Configured LoadingOverlay
     */
    public static LoadingOverlay createFullScreen(String message) {
        LoadingOverlay overlay = new LoadingOverlay(message);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return overlay;
    }
}
