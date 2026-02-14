package com.inventory.ui;

import com.inventory.model.User;
import com.inventory.service.UserService;
import com.inventory.util.ThemeManager;

import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXLoginApp extends Application {

    public static final String APP_NAME = "SmartStock Offline";
    private final UserService userService = new UserService();

    @Override
    public void start(Stage stage) {

        // Title
        Label title = new Label(APP_NAME);
title.getStyleClass().add("title-label");


        // Username
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        // Password
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        // Login button
        Image loginIcon = new Image("file:resources/icons/login.png");
ImageView loginView = new ImageView(loginIcon);
loginView.setFitWidth(18);
loginView.setFitHeight(18);

Button loginBtn = new Button("Login", loginView);


        Label resultLabel = new Label();
resultLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setDefaultButton(true);


        CheckBox darkModeCheck = new CheckBox("Dark Mode");
        darkModeCheck.setSelected(ThemeManager.isDarkMode());

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        grid.add(title, 0, 0, 2, 1);
        GridPane.setHalignment(title, HPos.CENTER);
        grid.add(userLabel, 0, 1);
        grid.add(userField, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(passField, 1, 2);
        grid.add(loginBtn, 1, 3);
        grid.add(resultLabel, 1, 4);
        grid.add(darkModeCheck, 1, 5);


        // Login logic
        loginBtn.setOnAction(e -> {

            String username = userField.getText().trim();
            String password = passField.getText().trim();

            // Disable button while checking
            loginBtn.setDisable(true);
            loginBtn.setText("Checking...");

            User user = userService.login(username, password);

            // Enable button again
            loginBtn.setDisable(false);
            loginBtn.setText("Login");

            if (user == null) {
    resultLabel.setText("Invalid username or password");
    return;
}
resultLabel.setText("");

            showAlert(Alert.AlertType.INFORMATION,
                    "Login Successful",
                    "Welcome to " + APP_NAME + ", " + user.getUsername());

            DashboardUI.open(user, stage);

        });

VBox loginCard = new VBox(grid);
loginCard.setAlignment(Pos.CENTER);
loginCard.setMaxWidth(520);
loginCard.setMaxHeight(Region.USE_PREF_SIZE);
loginCard.getStyleClass().add("panel-surface");

StackPane root = new StackPane(loginCard);
root.setAlignment(Pos.CENTER);
root.setPadding(new Insets(20));
root.getStyleClass().add("login-root");

Scene scene = new Scene(root, 500, 350);
scene.getStylesheets().add("file:style.css");
ThemeManager.applyTheme(scene);

darkModeCheck.setOnAction(e -> {
    ThemeManager.setDarkMode(darkModeCheck.isSelected());
    ThemeManager.applyTheme(scene);
});




        stage.setTitle(APP_NAME + " - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        userField.requestFocus();
        stage.setResizable(true);

    }

    // Alert helper method
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

