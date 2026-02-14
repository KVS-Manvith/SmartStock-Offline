package com.inventory.ui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class ProductUI {

    public static void open() {
        open(false);
    }

    public static void openReadOnly() {
        open(true);
    }

    // Backward-compatible entry points for older console classes.
    public static void show() {
        open(false);
    }

    public static void updateStock(int productId, int qty) {
        ProductService service = new ProductService();
        boolean updated = service.updateStock(productId, qty);
        if (!updated) {
            System.out.println("Failed to update stock for product id: " + productId);
        }
    }

    private static void open(boolean readOnly) {

        Stage stage = new Stage();
        stage.setTitle(readOnly ? "Products (Read Only)" : "Products");

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or category...");
        searchField.setPrefWidth(250);

        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        Button addBtn = new Button("Add Product");

        HBox searchBox = new HBox(10, searchField, searchBtn, refreshBtn, addBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10));

        // Table
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setPrefWidth(50);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price (Rs.)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);
        priceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("Rs. %.2f", item));
            }
        });

        TableColumn<Product, LocalDate> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        expiryCol.setPrefWidth(100);
        expiryCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-" : item.toString());
            }
        });

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(categoryCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(expiryCol);

        // Load data
        ProductDAO dao = new ProductDAO();
        ObservableList<Product> data = FXCollections.observableArrayList(dao.getAllProducts());
        table.setItems(data);

        // Action buttons
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button backBtn = new Button("Back");

        HBox actionBox = new HBox(10, editBtn, deleteBtn);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(10));
        HBox navBox = new HBox(backBtn);
        navBox.setAlignment(Pos.CENTER_RIGHT);
        navBox.setPadding(new Insets(0, 10, 10, 10));

        if (readOnly) {
            addBtn.setDisable(true);
            addBtn.setVisible(false);
            addBtn.setManaged(false);
            actionBox.setVisible(false);
            actionBox.setManaged(false);
        }

        // Button actions
        addBtn.setOnAction(e -> {
            if (readOnly) {
                return;
            }
            AddProductUI.open();
            // Refresh table after adding
            data.setAll(dao.getAllProducts());
        });

        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                data.setAll(dao.getAllProducts());
            } else {
                data.setAll(dao.searchProducts(keyword));
            }
        });

        refreshBtn.setOnAction(e -> {
            searchField.clear();
            data.setAll(dao.getAllProducts());
        });

        editBtn.setOnAction(e -> {
            if (readOnly) {
                return;
            }
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
                return;
            }

            AddProductUI.open(selected);
            // Refresh table after editing
            data.setAll(dao.getAllProducts());
        });

        deleteBtn.setOnAction(e -> {
            if (readOnly) {
                return;
            }
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete: " + selected.getName() + "?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ProductService service = new ProductService();
                boolean deleted = service.deleteProduct(selected.getProductId());
                data.setAll(dao.getAllProducts());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Product deleted successfully!");
                } else {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Delete Failed",
                            "Product could not be deleted. It may be referenced by sales history."
                    );
                }
            }
        });

        backBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(10, searchBox, table, actionBox, navBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 750, 500);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.show();
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
