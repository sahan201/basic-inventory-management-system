package com.example.dummy_inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // This line is the most important part.
        // It loads our LoginView.fxml file from the /resources/fxml/ folder.
        // The leading "/" tells it to look at the root of the resources' folder.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Inventory Management System - Login");
        primaryStage.setScene(new Scene(root, 350, 300)); // Set dimensions
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}