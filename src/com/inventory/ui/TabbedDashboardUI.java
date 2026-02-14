package com.inventory.ui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.User;
import com.inventory.service.ProductService;
import com.inventory.service.SaleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TabbedDashboardUI {

    private final User user;
    private final Stage stage;

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductService productService = new ProductService();
    private final SaleService saleService = new SaleService();

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<Sale> salesData = FXCollections.observableArrayList();

    private TableView<Product> productTable;
    private ComboBox<Product> billingProductCombo;
    private Spinner<Integer> billingQtySpinner;
    private Label billingPriceLabel;
    private Label billingTotalLabel;
    private Button processSaleBtn;
    private Label totalSalesLabel;

    private TabbedDashboardUI(User user, Stage stage) {
        this.user = user;
        this.stage = stage;
    }

    public static void show(User user, Stage stage) {
        new TabbedDashboardUI(user, stage).build();
    }

    private void build() {
        TabPane tabs = new TabPane();

        Tab productsTab = createProductsTab();
        Tab salesTab = createSalesTab();
        Tab reportsTab = createReportsTab(isAdmin() ? "Reports" : "Repport");
        Tab alertsTab = createAlertsTab(isAdmin() ? "Alerts" : "Alert");
        Tab accountTab = createAccountTab();
        Tab billingTab = createBillingTab(tabs, salesTab);

        if (isAdmin()) {
            // Admin: Products, Sales History, Reports, Alerts, Account
            tabs.getTabs().addAll(productsTab, salesTab, reportsTab, alertsTab, accountTab);
        } else {
            // Staff: Products, Account, Billing, Sales History, Alert, Repport
            tabs.getTabs().addAll(productsTab, accountTab, billingTab, salesTab, alertsTab, reportsTab);
        }

        VBox root = new VBox(10,
                buildHeader(),
                tabs
        );
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add("file:style.css");

        stage.setTitle(FXLoginApp.APP_NAME + " - " + (isAdmin() ? "Admin Dashboard" : "Staff Dashboard"));
        stage.getIcons().add(new Image("file:" + getRoleIconPath()));
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        refreshProducts();
        refreshSales();
    }

    private HBox buildHeader() {
        ImageView logoView = createIcon("resources/icons/product.png", 34, 34);
        ImageView roleIconView = createIcon(getRoleIconPath(), 34, 34);

        Label title = new Label(FXLoginApp.APP_NAME + " - " + (isAdmin() ? "Admin Dashboard" : "Staff Dashboard"));
        title.getStyleClass().add("dashboard-title");

        Label welcome = new Label(
                "Logged in as: "
                        + user.getUsername() + " (" + user.getRole() + ")"
        );

        HBox header = new HBox(12, logoView, roleIconView, title, welcome);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(5, 5, 10, 5));
        return header;
    }

    private Tab createProductsTab() {
        Tab tab = new Tab("Products");
        tab.setClosable(false);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or category...");
        searchField.setPrefWidth(280);

        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");

        HBox topBar = new HBox(10, searchField, searchBtn, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        productTable = createProductTable();
        productTable.setItems(productData);

        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
            if (keyword.isEmpty()) {
                refreshProducts();
            } else {
                productData.setAll(productDAO.searchProducts(keyword));
            }
        });
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            refreshProducts();
        });

        VBox content = new VBox(10, topBar, productTable);
        content.setPadding(new Insets(10));
        VBox.setVgrow(productTable, Priority.ALWAYS);

        if (isAdmin()) {
            content.getChildren().add(createProductFormPane());
        }

        tab.setContent(content);
        return tab;
    }

    private VBox createProductFormPane() {
        Label formTitle = new Label("Manage Product");
        formTitle.getStyleClass().add("title-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Dairy", "Bakery", "Meat", "Grains", "Beverages", "Other");
        categoryCombo.setEditable(true);
        categoryCombo.setPromptText("Category");

        Spinner<Integer> qtySpinner = new Spinner<>(0, 100000, 0);
        qtySpinner.setEditable(true);

        TextField priceField = new TextField();
        priceField.setPromptText("Price (Rs.)");

        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setPromptText("Expiry (Optional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category*"), 2, 0);
        grid.add(categoryCombo, 3, 0);
        grid.add(new Label("Quantity*"), 0, 1);
        grid.add(qtySpinner, 1, 1);
        grid.add(new Label("Price (Rs.)*"), 2, 1);
        grid.add(priceField, 3, 1);
        grid.add(new Label("Expiry Date"), 0, 2);
        grid.add(expiryPicker, 1, 2);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete Selected");
        Button clearBtn = new Button("Clear");

        HBox actions = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                return;
            }
            nameField.setText(selected.getName());
            categoryCombo.setValue(selected.getCategory());
            qtySpinner.getValueFactory().setValue(selected.getQuantity());
            priceField.setText(String.valueOf(selected.getPrice()));
            expiryPicker.setValue(selected.getExpiryDate());
        });

        addBtn.setOnAction(e -> {
            Product p = buildProductFromForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker);
            if (p == null) {
                return;
            }
            productService.addProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added.");
            clearProductForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker);
            refreshProducts();
        });

        updateBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Select a product to update.");
                return;
            }
            Product p = buildProductFromForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker);
            if (p == null) {
                return;
            }
            p.setProductId(selected.getProductId());
            productService.updateProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated.");
            clearProductForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker);
            refreshProducts();
        });

        deleteBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Select a product to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            confirm.setContentText("Delete product: " + selected.getName() + "?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            boolean deleted = productService.deleteProduct(selected.getProductId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Product deleted.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete product (possibly linked in sales).");
            }
            clearProductForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker);
            refreshProducts();
        });

        clearBtn.setOnAction(e -> clearProductForm(nameField, categoryCombo, qtySpinner, priceField, expiryPicker));

        VBox wrapper = new VBox(10, new Separator(), formTitle, grid, actions);
        wrapper.setPadding(new Insets(10));
        return wrapper;
    }

    private Product buildProductFromForm(
            TextField nameField,
            ComboBox<String> categoryCombo,
            Spinner<Integer> qtySpinner,
            TextField priceField,
            DatePicker expiryPicker
    ) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String category = categoryCombo.getValue() == null ? "" : categoryCombo.getValue().trim();
        String priceText = priceField.getText() == null ? "" : priceField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name, category, and price are required.");
            return null;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be numeric.");
            return null;
        }

        if (price < 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be non-negative.");
            return null;
        }

        Product p = new Product();
        p.setName(name);
        p.setCategory(category);
        p.setQuantity(qtySpinner.getValue());
        p.setPrice(price);
        p.setExpiryDate(expiryPicker.getValue());
        return p;
    }

    private void clearProductForm(
            TextField nameField,
            ComboBox<String> categoryCombo,
            Spinner<Integer> qtySpinner,
            TextField priceField,
            DatePicker expiryPicker
    ) {
        productTable.getSelectionModel().clearSelection();
        nameField.clear();
        categoryCombo.setValue(null);
        qtySpinner.getValueFactory().setValue(0);
        priceField.clear();
        expiryPicker.setValue(null);
    }

    private Tab createBillingTab(TabPane tabPane, Tab salesTab) {
        Tab tab = new Tab("Billing");
        tab.setClosable(false);

        billingProductCombo = new ComboBox<>();
        billingProductCombo.setPrefWidth(420);
        billingProductCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + formatCurrency(item.getPrice()) + " (Stock: " + item.getQuantity() + ")");
                }
            }
        });
        billingProductCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + formatCurrency(item.getPrice()) + " (Stock: " + item.getQuantity() + ")");
                }
            }
        });

        billingQtySpinner = new Spinner<>(0, 0, 0);
        billingQtySpinner.setEditable(true);
        billingQtySpinner.setDisable(true);

        billingPriceLabel = new Label("Unit Price: Rs. 0.00");
        billingTotalLabel = new Label("Total: Rs. 0.00");
        billingTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        processSaleBtn = new Button("Process Sale");
        processSaleBtn.setDisable(true);
        Button openSalesBtn = new Button("Open Sales History Tab");

        billingProductCombo.setOnAction(e -> updateBillingSelectionState());
        billingQtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateBillingTotal());

        processSaleBtn.setOnAction(e -> processSale());
        openSalesBtn.setOnAction(e -> tabPane.getSelectionModel().select(salesTab));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Product"), 0, 0);
        grid.add(billingProductCombo, 1, 0);
        grid.add(new Label("Quantity"), 0, 1);
        grid.add(billingQtySpinner, 1, 1);

        HBox actions = new HBox(10, processSaleBtn, openSalesBtn);

        VBox content = new VBox(15,
                new Label("Process Sale"),
                grid,
                billingPriceLabel,
                billingTotalLabel,
                actions
        );
        content.setPadding(new Insets(20));
        tab.setContent(content);
        return tab;
    }

    private Tab createSalesTab() {
        Tab tab = new Tab("Sales History");
        tab.setClosable(false);

        TableView<Sale> salesTable = new TableView<>();
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));

        TableColumn<Sale, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Sale, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));

        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total (Rs.)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        TableColumn<Sale, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        dateCol.setCellFactory(column -> new TableCell<>() {
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

        salesTable.getColumns().addAll(idCol, productCol, qtyCol, totalCol, dateCol);
        salesTable.setItems(salesData);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshSales());

        totalSalesLabel = new Label("Total Sales: Rs. 0.00");
        totalSalesLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox content = new VBox(10, refreshBtn, salesTable, totalSalesLabel);
        content.setPadding(new Insets(12));
        VBox.setVgrow(salesTable, Priority.ALWAYS);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                refreshSales();
            }
        });

        tab.setContent(content);
        return tab;
    }

    private Tab createReportsTab(String title) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        TabPane reportTabs = new TabPane();
        reportTabs.getTabs().add(createAllProductsReportTab());
        reportTabs.getTabs().add(createLowStockReportTab());
        reportTabs.getTabs().add(createExpiringReportTab());
        reportTabs.getTabs().add(createCategoryReportTab());

        tab.setContent(reportTabs);
        return tab;
    }

    private Tab createAllProductsReportTab() {
        Tab tab = new Tab("All Products");
        tab.setClosable(false);

        TableView<Product> table = createProductTable();
        Label countLabel = new Label();
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getAllProducts());
            table.setItems(data);
            countLabel.setText("Count: " + data.size());
        });

        refreshBtn.fire();

        VBox content = new VBox(10, refreshBtn, table, countLabel);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createLowStockReportTab() {
        Tab tab = new Tab("Low Stock");
        tab.setClosable(false);

        Spinner<Integer> thresholdSpinner = new Spinner<>(1, 500, 10);
        thresholdSpinner.setEditable(true);
        Button refreshBtn = new Button("Refresh");
        TableView<Product> table = createProductTable();
        Label countLabel = new Label();

        refreshBtn.setOnAction(e -> {
            int threshold = thresholdSpinner.getValue();
            ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getLowStockProducts(threshold));
            table.setItems(data);
            countLabel.setText("Low stock count: " + data.size());
        });
        refreshBtn.fire();

        HBox controls = new HBox(10, new Label("Threshold"), thresholdSpinner, refreshBtn);
        VBox content = new VBox(10, controls, table, countLabel);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createExpiringReportTab() {
        Tab tab = new Tab("Expiring Soon");
        tab.setClosable(false);

        Spinner<Integer> daysSpinner = new Spinner<>(1, 60, 7);
        daysSpinner.setEditable(true);
        Button refreshBtn = new Button("Refresh");
        TableView<Product> table = createProductTable();
        Label countLabel = new Label();

        refreshBtn.setOnAction(e -> {
            int days = daysSpinner.getValue();
            ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getNearExpiryProducts(days));
            table.setItems(data);
            countLabel.setText("Expiring count: " + data.size());
        });
        refreshBtn.fire();

        HBox controls = new HBox(10, new Label("Days"), daysSpinner, refreshBtn);
        VBox content = new VBox(10, controls, table, countLabel);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createCategoryReportTab() {
        Tab tab = new Tab("By Category");
        tab.setClosable(false);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All", "Dairy", "Bakery", "Meat", "Grains", "Beverages", "Other");
        categoryCombo.setValue("All");
        Button refreshBtn = new Button("Refresh");
        TableView<Product> table = createProductTable();
        Label countLabel = new Label();

        refreshBtn.setOnAction(e -> {
            String category = categoryCombo.getValue();
            ObservableList<Product> data = "All".equalsIgnoreCase(category)
                    ? FXCollections.observableArrayList(productDAO.getAllProducts())
                    : FXCollections.observableArrayList(productDAO.searchProducts(category));
            table.setItems(data);
            countLabel.setText("Count: " + data.size());
        });
        refreshBtn.fire();

        HBox controls = new HBox(10, new Label("Category"), categoryCombo, refreshBtn);
        VBox content = new VBox(10, controls, table, countLabel);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createAlertsTab(String title) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        TabPane alertTabs = new TabPane();
        alertTabs.getTabs().add(createLowStockAlertTab());
        alertTabs.getTabs().add(createExpiringAlertTab());
        tab.setContent(alertTabs);
        return tab;
    }

    private Tab createLowStockAlertTab() {
        Tab tab = new Tab("Low Stock");
        tab.setClosable(false);

        TableView<Product> table = createProductTable();
        Label label = new Label();
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getLowStockProducts(10));
            table.setItems(data);
            label.setText("Low stock items (<= 10): " + data.size());
        });
        refreshBtn.fire();

        VBox content = new VBox(10, refreshBtn, label, table);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createExpiringAlertTab() {
        Tab tab = new Tab("Expiring Soon");
        tab.setClosable(false);

        TableView<Product> table = createProductTable();
        Label label = new Label();
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            ObservableList<Product> data = FXCollections.observableArrayList(productDAO.getNearExpiryProducts(7));
            table.setItems(data);
            label.setText("Expiring in next 7 days: " + data.size());
        });
        refreshBtn.fire();

        VBox content = new VBox(10, refreshBtn, label, table);
        content.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private Tab createAccountTab() {
        Tab tab = new Tab("Account");
        tab.setClosable(false);

        Label username = new Label("Username: " + user.getUsername());
        Label role = new Label("Role: " + user.getRole());
        Button logoutBtn = new Button("Logout");

        logoutBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Logout");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to logout?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DashboardUI.openLogin(stage);
            }
        });

        VBox content = new VBox(15, username, role, logoutBtn);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);
        tab.setContent(content);
        return tab;
    }

    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price (Rs.)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        TableColumn<Product, LocalDate> expiryCol = new TableColumn<>("Expiry");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
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

    private void refreshProducts() {
        productData.setAll(productDAO.getAllProducts());

        if (billingProductCombo != null) {
            Product selected = billingProductCombo.getValue();
            billingProductCombo.setItems(FXCollections.observableArrayList(productData));

            if (selected != null) {
                Product replacement = productData.stream()
                        .filter(p -> p.getProductId() == selected.getProductId())
                        .findFirst()
                        .orElse(null);
                billingProductCombo.setValue(replacement);
            }
            updateBillingSelectionState();
        }
    }

    private void refreshSales() {
        salesData.setAll(saleService.getAllSales());
        if (totalSalesLabel != null) {
            totalSalesLabel.setText("Total Sales: " + formatCurrency(saleService.getTotalSalesAmount()));
        }
    }

    private void updateBillingSelectionState() {
        Product selected = billingProductCombo.getValue();
        if (selected == null) {
            billingQtySpinner.setDisable(true);
            billingQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            billingPriceLabel.setText("Unit Price: Rs. 0.00");
            billingTotalLabel.setText("Total: Rs. 0.00");
            processSaleBtn.setDisable(true);
            return;
        }

        billingPriceLabel.setText("Unit Price: " + formatCurrency(selected.getPrice()));
        if (selected.getQuantity() <= 0) {
            billingQtySpinner.setDisable(true);
            billingQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            billingTotalLabel.setText("Total: Rs. 0.00");
            processSaleBtn.setDisable(true);
        } else {
            billingQtySpinner.setDisable(false);
            billingQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, selected.getQuantity(), 1));
            processSaleBtn.setDisable(false);
            updateBillingTotal();
        }
    }

    private void updateBillingTotal() {
        Product selected = billingProductCombo.getValue();
        if (selected == null) {
            billingTotalLabel.setText("Total: Rs. 0.00");
            return;
        }
        int qty = billingQtySpinner.getValue();
        double total = selected.getPrice() * qty;
        billingTotalLabel.setText("Total: " + formatCurrency(total));
    }

    private void processSale() {
        Product selected = billingProductCombo.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Product", "Select a product.");
            return;
        }

        int qty = billingQtySpinner.getValue();
        if (qty <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than zero.");
            return;
        }

        boolean success = saleService.processSale(selected.getProductId(), qty, user.getUserId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Sale Successful", "Sale processed.");
            refreshProducts();
            refreshSales();
        } else {
            showAlert(Alert.AlertType.ERROR, "Sale Failed", "Could not process sale.");
        }
    }

    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private String getRoleIconPath() {
        return isAdmin() ? "resources/icons/admin.png" : "resources/icons/staff.png";
    }

    private ImageView createIcon(String path, double width, double height) {
        ImageView view = new ImageView(new Image("file:" + path));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(true);
        return view;
    }

    private static String formatCurrency(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
