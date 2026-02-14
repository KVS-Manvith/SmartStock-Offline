package com.inventory.ui;

import java.util.Optional;

import com.inventory.model.User;
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

public class AdminDashboard {

    public static void show(User user) {

        Label title = new Label("ADMIN DASHBOARD");
        title.getStyleClass().add("dashboard-title");

        Label welcome = new Label("Welcome, " + user.getUsername());

        Button addProductBtn = createButton("Add Product", "resources/icons/product.png");
        Button viewProductBtn = createButton("View Products", "resources/icons/product.png");
        Button billingBtn = createButton("Billing / Sales", "resources/icons/product.png");
        Button reportBtn = createButton("Reports", "resources/icons/report.png");
        Button alertsBtn = createButton("Alerts", "resources/icons/report.png");
        Button logoutBtn = createButton("Logout", "resources/icons/logout.png");

        // ✅ BUTTON ACTIONS (INSIDE METHOD)
        addProductBtn.setOnAction(e -> AddProductUI.open());
        viewProductBtn.setOnAction(e -> ProductUI.open());
        billingBtn.setOnAction(e -> BillingUI.open(user));
        reportBtn.setOnAction(e -> ReportUI.open());
        alertsBtn.setOnAction(e -> AlertUI.open());

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

        VBox layout = new VBox(15,
                title,
                welcome,
                addProductBtn,
                viewProductBtn,
                billingBtn,
                reportBtn,
                alertsBtn,
                logoutBtn
        );

        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30;");

        Scene scene = new Scene(layout, 420, 500);
        scene.getStylesheets().add("file:style.css");

        // ✅ KEYBOARD SHORTCUTS (INSIDE METHOD)
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case L -> logoutBtn.fire();
                case R -> reportBtn.fire();
                case B -> billingBtn.fire();
            }
        });

        Stage stage = new Stage();
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // ---------- ICON HELPER ----------
    private static Button createButton(String text, String iconPath) {

        Button btn = new Button(text);
        btn.setPrefWidth(220);

        try {
            Image iconImage = new Image("file:" + iconPath);

            ImageView icon = new ImageView(iconImage);
            icon.setFitWidth(18);
            icon.setFitHeight(18);
            icon.setPreserveRatio(true);

            btn.setGraphic(icon);

        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }

        return btn;
    }
}
