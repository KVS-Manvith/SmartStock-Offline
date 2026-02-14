package com.inventory.ui;

import com.inventory.model.Sale;
import com.inventory.service.SaleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesHistoryUI {

    public static void open() {

        Stage stage = new Stage();
        stage.setTitle("Sales History");

        // Table
        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        idCol.setPrefWidth(80);

        TableColumn<Sale, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.setPrefWidth(150);

        TableColumn<Sale, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        qtyCol.setPrefWidth(80);

        TableColumn<Sale, Double> priceCol = new TableColumn<>("Total Price (Rs.)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("Rs. %.2f", item));
            }
        });

        TableColumn<Sale, LocalDateTime> dateCol = new TableColumn<>("Sale Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        dateCol.setPrefWidth(150);

        // Format date column
        dateCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            }
        });

        table.getColumns().add(idCol);
        table.getColumns().add(productCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(dateCol);

        // Load data
        SaleService saleService = new SaleService();
        ObservableList<Sale> sales = FXCollections.observableArrayList(saleService.getAllSales());
        table.setItems(sales);

        // Total sales
        double totalSales = saleService.getTotalSalesAmount();
        Label totalLabel = new Label("Total Sales: Rs. " + String.format("%.2f", totalSales));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> stage.close());
        HBox navBox = new HBox(backBtn);
        navBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10, new Label("Sales History"), table, totalLabel, navBox);
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 700, 500);
        scene.getStylesheets().add("file:style.css");

        stage.setScene(scene);
        stage.show();
    }
}
