package com.inventory.ui;

import com.inventory.model.Product;
import com.inventory.service.ProductService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddProductUI {

    public static void open() {
        open(null);
    }

    public static void open(Product existingProduct) {

        Stage stage = new Stage();
        stage.setTitle(existingProduct == null ? "Add Product" : "Edit Product");

        // Form fields
        Label nameLabel = new Label("Product Name:*");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter product name");

        Label categoryLabel = new Label("Category:*");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Dairy", "Bakery", "Meat", "Grains", "Beverages", "Other");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setEditable(true);

        Label qtyLabel = new Label("Quantity:*");
        Spinner<Integer> qtySpinner = new Spinner<>(0, 10000, 0);
        qtySpinner.setEditable(true);

        Label priceLabel = new Label("Price (Rs.):*");
        TextField priceField = new TextField();
        priceField.setPromptText("0.00");

        Label expiryLabel = new Label("Expiry Date:");
        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setPromptText("Optional");

        // If editing, populate fields
        if (existingProduct != null) {
            nameField.setText(existingProduct.getName());
            categoryCombo.setValue(existingProduct.getCategory());
            qtySpinner.getValueFactory().setValue(existingProduct.getQuantity());
            priceField.setText(String.valueOf(existingProduct.getPrice()));
            if (existingProduct.getExpiryDate() != null) {
                expiryPicker.setValue(existingProduct.getExpiryDate());
            }
        }

        // Buttons
        Button saveBtn = new Button(existingProduct == null ? "Add Product" : "Update Product");
        Button cancelBtn = new Button("Back");

        saveBtn.setOnAction(e -> {

            // Validation
            if (nameField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Product name is required.");
                return;
            }

            if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Category is required.");
                return;
            }

            if (priceField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Price is required.");
                return;
            }

            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Price must be positive.");
                    return;
                }

                // Create or update product
                Product product = existingProduct != null ? existingProduct : new Product();
                product.setName(nameField.getText().trim());
                product.setCategory(categoryCombo.getValue().trim());
                product.setQuantity(qtySpinner.getValue());
                product.setPrice(price);
                product.setExpiryDate(expiryPicker.getValue());

                ProductService service = new ProductService();

                if (existingProduct == null) {
                    service.addProduct(product);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
                } else {
                    service.updateProduct(product);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                }

                stage.close();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price must be a valid number.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(categoryLabel, 0, 1);
        grid.add(categoryCombo, 1, 1);

        grid.add(qtyLabel, 0, 2);
        grid.add(qtySpinner, 1, 2);

        grid.add(priceLabel, 0, 3);
        grid.add(priceField, 1, 3);

        grid.add(expiryLabel, 0, 4);
        grid.add(expiryPicker, 1, 4);

        HBox buttonBox = new HBox(15, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(20, grid, buttonBox);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 450, 400);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
