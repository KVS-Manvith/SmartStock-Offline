package com.inventory.ui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.model.User;
import com.inventory.service.SaleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BillingUI {

    private static User currentUser;

    public static void open(User user) {
        currentUser = user;

        Stage stage = new Stage();
        stage.setTitle("Billing / Sales");

        // Product selection
        Label productLabel = new Label("Select Product:");
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setPrefWidth(250);

        // Load products
        ProductDAO productDAO = new ProductDAO();
        ObservableList<Product> products = FXCollections.observableArrayList(productDAO.getAllProducts());
        productCombo.setItems(products);

        // Custom display for products in combo box
        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - Rs. " + String.format("%.2f", item.getPrice()) + " (Stock: " + item.getQuantity() + ")");
                }
            }
        });

        productCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - Rs. " + String.format("%.2f", item.getPrice()) + " (Stock: " + item.getQuantity() + ")");
                }
            }
        });

        // Quantity
        Label qtyLabel = new Label("Quantity:");
        Spinner<Integer> qtySpinner = new Spinner<>(0, 0, 0);
        qtySpinner.setEditable(true);
        qtySpinner.setPrefWidth(100);
        qtySpinner.setDisable(true);

        // Price display
        Label priceLabel = new Label("Unit Price: Rs. 0.00");
        Label totalLabel = new Label("Total: Rs. 0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Buttons
        Button processBtn = new Button("Process Sale");
        Button viewSalesBtn = new Button("View Sales History");
        Button backBtn = new Button("Back");
        processBtn.setDisable(true);

        // Update price when product or quantity changes
        productCombo.setOnAction(e -> {
            Product selected = productCombo.getValue();
            if (selected != null) {
                priceLabel.setText("Unit Price: Rs. " + String.format("%.2f", selected.getPrice()));
                if (selected.getQuantity() <= 0) {
                    qtySpinner.setDisable(true);
                    qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
                    totalLabel.setText("Total: Rs. 0.00");
                    processBtn.setDisable(true);
                } else {
                    qtySpinner.setDisable(false);
                    qtySpinner.setValueFactory(
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, selected.getQuantity(), 1)
                    );
                    updateTotal(selected, qtySpinner.getValue(), totalLabel);
                    processBtn.setDisable(false);
                }
            }
        });

        qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            Product selected = productCombo.getValue();
            if (selected != null && selected.getQuantity() > 0) {
                updateTotal(selected, newVal, totalLabel);
            }
        });

        processBtn.setOnAction(e -> {
            Product selected = productCombo.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product.");
                return;
            }

            int quantity = qtySpinner.getValue();
            if (quantity <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Please choose a quantity greater than zero.");
                return;
            }

            SaleService saleService = new SaleService();
            boolean success = saleService.processSale(selected.getProductId(), quantity, currentUser.getUserId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sale Successful",
                        "Sale processed successfully!\nTotal: Rs. " + String.format("%.2f", selected.getPrice() * quantity));

                // Refresh product list
                products.setAll(productDAO.getAllProducts());
                productCombo.setValue(null);
                qtySpinner.setDisable(true);
                qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
                processBtn.setDisable(true);
                priceLabel.setText("Unit Price: Rs. 0.00");
                totalLabel.setText("Total: Rs. 0.00");
            } else {
                showAlert(Alert.AlertType.ERROR, "Sale Failed", "Sale could not be processed. Please check stock availability.");
            }
        });

        viewSalesBtn.setOnAction(e -> SalesHistoryUI.open());

        backBtn.setOnAction(e -> stage.close());

        // Layout
        VBox productBox = new VBox(10, productLabel, productCombo);
        VBox qtyBox = new VBox(10, qtyLabel, qtySpinner);

        HBox inputRow = new HBox(20, productBox, qtyBox);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        HBox buttonRow = new HBox(15, processBtn, viewSalesBtn, backBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(20,
                new Label("Process Sale"),
                new Separator(),
                inputRow,
                priceLabel,
                totalLabel,
                new Separator(),
                buttonRow
        );

        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.show();
    }

    private static void updateTotal(Product product, int quantity, Label totalLabel) {
        double total = product.getPrice() * quantity;
        totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
