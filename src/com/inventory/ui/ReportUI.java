package com.inventory.ui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
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

public class ReportUI {

    public static void open() {

        Stage stage = new Stage();
        stage.setTitle("Reports");

        // Tab Pane
        TabPane tabPane = new TabPane();

        // Tab 1: All Products
        Tab allProductsTab = new Tab("All Products");
        allProductsTab.setClosable(false);
        allProductsTab.setContent(createAllProductsView());

        // Tab 2: Low Stock
        Tab lowStockTab = new Tab("Low Stock");
        lowStockTab.setClosable(false);
        lowStockTab.setContent(createLowStockView());

        // Tab 3: Expiring Soon
        Tab expiringTab = new Tab("Expiring Soon");
        expiringTab.setClosable(false);
        expiringTab.setContent(createExpiringView());

        // Tab 4: Category Report
        Tab categoryTab = new Tab("By Category");
        categoryTab.setClosable(false);
        categoryTab.setContent(createCategoryView());

        tabPane.getTabs().addAll(allProductsTab, lowStockTab, expiringTab, categoryTab);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> stage.close());
        HBox navBox = new HBox(backBtn);
        navBox.setAlignment(Pos.CENTER_RIGHT);
        navBox.setPadding(new Insets(10));

        VBox root = new VBox(tabPane, navBox);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.show();
    }

    // All Products Report
    private static VBox createAllProductsView() {

        TableView<Product> table = createProductTable();

        ProductDAO dao = new ProductDAO();
        ObservableList<Product> products = FXCollections.observableArrayList(dao.getAllProducts());
        table.setItems(products);

        Label totalLabel = new Label("Total Products: " + products.size());
        totalLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10;");

        VBox layout = new VBox(10, table, totalLabel);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Low Stock Report
    private static VBox createLowStockView() {

        TableView<Product> table = createProductTable();

        Label thresholdLabel = new Label("Stock Threshold:");
        Spinner<Integer> thresholdSpinner = new Spinner<>(1, 100, 10);
        thresholdSpinner.setEditable(true);
        thresholdSpinner.setPrefWidth(100);

        Button refreshBtn = new Button("Refresh");

        HBox controls = new HBox(15, thresholdLabel, thresholdSpinner, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        ProductDAO dao = new ProductDAO();

        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-text-fill: red;");

        refreshBtn.setOnAction(e -> {
            int threshold = thresholdSpinner.getValue();
            ObservableList<Product> lowStock = FXCollections.observableArrayList(dao.getLowStockProducts(threshold));
            table.setItems(lowStock);
            countLabel.setText("Low Stock Products: " + lowStock.size());
        });

        // Initial load
        ObservableList<Product> lowStock = FXCollections.observableArrayList(dao.getLowStockProducts(10));
        table.setItems(lowStock);
        countLabel.setText("Low Stock Products: " + lowStock.size());

        VBox layout = new VBox(10, controls, table, countLabel);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Expiring Soon Report
    private static VBox createExpiringView() {

        TableView<Product> table = createProductTable();

        Label daysLabel = new Label("Days:");
        Spinner<Integer> daysSpinner = new Spinner<>(1, 30, 7);
        daysSpinner.setEditable(true);
        daysSpinner.setPrefWidth(100);

        Button refreshBtn = new Button("Refresh");

        HBox controls = new HBox(15, daysLabel, daysSpinner, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        ProductDAO dao = new ProductDAO();

        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-text-fill: orange;");

        refreshBtn.setOnAction(e -> {
            int days = daysSpinner.getValue();
            ObservableList<Product> expiring = FXCollections.observableArrayList(dao.getNearExpiryProducts(days));
            table.setItems(expiring);
            countLabel.setText("Expiring Soon: " + expiring.size());
        });

        // Initial load
        ObservableList<Product> expiring = FXCollections.observableArrayList(dao.getNearExpiryProducts(7));
        table.setItems(expiring);
        countLabel.setText("Expiring Soon: " + expiring.size());

        VBox layout = new VBox(10, controls, table, countLabel);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Category Report
    private static VBox createCategoryView() {

        TableView<Product> table = createProductTable();

        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All", "Dairy", "Bakery", "Meat", "Grains", "Beverages");
        categoryCombo.setValue("All");
        categoryCombo.setPrefWidth(150);

        Button refreshBtn = new Button("Refresh");

        HBox controls = new HBox(15, categoryLabel, categoryCombo, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        ProductDAO dao = new ProductDAO();

        refreshBtn.setOnAction(e -> {
            String category = categoryCombo.getValue();
            ObservableList<Product> products;

            if (category.equals("All")) {
                products = FXCollections.observableArrayList(dao.getAllProducts());
            } else {
                products = FXCollections.observableArrayList(dao.searchProducts(category));
            }

            table.setItems(products);
        });

        // Initial load
        ObservableList<Product> products = FXCollections.observableArrayList(dao.getAllProducts());
        table.setItems(products);

        VBox layout = new VBox(10, controls, table);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Helper method to create product table
    private static TableView<Product> createProductTable() {

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        table.getColumns().addAll(idCol, nameCol, categoryCol, qtyCol, priceCol, expiryCol);

        return table;
    }
}


