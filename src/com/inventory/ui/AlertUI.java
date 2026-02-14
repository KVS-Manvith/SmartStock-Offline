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

public class AlertUI {

    public static void open() {

        Stage stage = new Stage();
        stage.setTitle("Alerts");

        // Tab Pane
        TabPane tabPane = new TabPane();

        // Tab 1: Low Stock
        Tab lowStockTab = new Tab("Low Stock Alerts");
        lowStockTab.setClosable(false);
        lowStockTab.setContent(createLowStockView());

        // Tab 2: Expiring Soon
        Tab expiringTab = new Tab("Expiring Soon");
        expiringTab.setClosable(false);
        expiringTab.setContent(createExpiringView());

        tabPane.getTabs().addAll(lowStockTab, expiringTab);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> stage.close());
        HBox navBox = new HBox(backBtn);
        navBox.setAlignment(Pos.CENTER_RIGHT);
        navBox.setPadding(new Insets(10));

        VBox root = new VBox(tabPane, navBox);
        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.show();
    }

    // Low Stock Alert View
    private static VBox createLowStockView() {

        TableView<Product> table = createAlertTable();

        ProductDAO dao = new ProductDAO();
        ObservableList<Product> lowStock = FXCollections.observableArrayList(dao.getLowStockProducts(10));
        table.setItems(lowStock);

        Label countLabel = new Label("⚠ Low Stock Items: " + lowStock.size());
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red; -fx-padding: 10;");

        Label infoLabel = new Label("Products with quantity less than or equal to 10 units");
        infoLabel.setStyle("-fx-padding: 5; -fx-text-fill: gray;");

        VBox layout = new VBox(10, countLabel, infoLabel, table);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Expiring Soon Alert View
    private static VBox createExpiringView() {

        TableView<Product> table = createAlertTable();

        ProductDAO dao = new ProductDAO();
        ObservableList<Product> expiring = FXCollections.observableArrayList(dao.getNearExpiryProducts(7));
        table.setItems(expiring);

        Label countLabel = new Label("⚠ Products Expiring Soon: " + expiring.size());
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: orange; -fx-padding: 10;");

        Label infoLabel = new Label("Products expiring within the next 7 days");
        infoLabel.setStyle("-fx-padding: 5; -fx-text-fill: gray;");

        VBox layout = new VBox(10, countLabel, infoLabel, table);
        layout.setPadding(new Insets(15));
        return layout;
    }

    // Helper method to create alert table
    private static TableView<Product> createAlertTable() {

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

        // Style low quantity cells
        qtyCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item <= 10) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

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

        // Style expiry date cells
        expiryCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    LocalDate today = LocalDate.now();
                    if (item.isBefore(today)) {
                        setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if (item.isBefore(today.plusDays(7))) {
                        setStyle("-fx-background-color: #ffcc99; -fx-text-fill: black; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, categoryCol, qtyCol, priceCol, expiryCol);

        return table;
    }
}
