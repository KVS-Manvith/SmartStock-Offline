package com.inventory.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class StaffDashboard {

    public static void open() {

        Label title = new Label("Staff Dashboard");
        title.getStyleClass().add("title-label");

        // View Products Button with icon
        ImageView productIcon = new ImageView(
                new Image("file:resources/icons/product.png")
        );
        productIcon.setFitWidth(18);
        productIcon.setFitHeight(18);

        Button viewBtn = new Button("View Products", productIcon);

        // Logout Button with icon
        ImageView logoutIcon = new ImageView(
                new Image("file:resources/icons/logout.png")
        );
        logoutIcon.setFitWidth(18);
        logoutIcon.setFitHeight(18);

        Button logoutBtn = new Button("Logout", logoutIcon);

        // Button actions
        viewBtn.setOnAction(e -> ProductUI.openReadOnly());

        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Logout");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to logout?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Stage currentStage = ((Stage) logoutBtn.getScene().getWindow());
                currentStage.close();
                DashboardUI.openLogin(new Stage());
            }
        });

        VBox layout = new VBox(15, title, viewBtn, logoutBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 400, 250);
        scene.getStylesheets().add("file:style.css");

        Stage stage = new Stage();
        stage.setTitle("Staff Dashboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }
}
