package com.inventory.ui;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.User;
import com.inventory.service.ProductService;
import com.inventory.service.SaleService;
import com.inventory.util.ThemeManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TabbedDashboardUI {

    private final User user;
    private final Stage stage;

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductService productService = new ProductService();
    private final SaleService saleService = new SaleService();

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private final ObservableList<Sale> salesData = FXCollections.observableArrayList();
    private final ObservableList<BillingCartItem> billingCartData = FXCollections.observableArrayList();
    private Scene scene;

    private TableView<Product> productTable;
    private TableView<BillingCartItem> billingCartTable;
    private ComboBox<Product> billingProductCombo;
    private Spinner<Integer> billingQtySpinner;
    private Spinner<Double> billingDiscountSpinner;
    private TextField billingPaidField;
    private Label billingPriceLabel;
    private Label billingSubtotalLabel;
    private Label billingDiscountLabel;
    private Label billingTotalLabel;
    private Label billingChangeLabel;
    private Label billingCartCountLabel;
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
        root.getStyleClass().add("dashboard-root");
        root.setPadding(new Insets(10));

        scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add("file:style.css");
        ThemeManager.applyTheme(scene);

        stage.setTitle(FXLoginApp.APP_NAME + " - " + (isAdmin() ? "Admin Dashboard" : "Staff Dashboard"));
        stage.getIcons().add(new Image("file:" + getRoleIconPath()));
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
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

        ToggleButton darkModeToggle = new ToggleButton("Dark Mode");
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        darkModeToggle.setOnAction(e -> {
            ThemeManager.setDarkMode(darkModeToggle.isSelected());
            ThemeManager.applyTheme(scene);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, logoView, roleIconView, title, welcome, spacer, darkModeToggle);
        header.getStyleClass().add("dashboard-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(5, 5, 10, 5));
        return header;
    }

    private Tab createProductsTab() {
        Tab tab = new Tab("Products");
        tab.setClosable(false);
        tab.setDisable(false);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or category...");
        searchField.setPrefWidth(280);

        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");

        HBox topBar = new HBox(10, searchField, searchBtn, refreshBtn);
        topBar.getStyleClass().add("toolbar-row");
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
            productTable.setDisable(false);
        });
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            refreshProducts();
            productTable.setDisable(false);
        });

        VBox content = new VBox(10, topBar, productTable);
        content.getStyleClass().add("panel-surface");
        content.setPadding(new Insets(10));
        VBox.setVgrow(productTable, Priority.ALWAYS);

        if (isAdmin()) {
            content.getChildren().add(createProductFormPane());
        }

        // Defensive reset: if Products view ever enters a disabled state, restore it on tab focus.
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                tab.setDisable(false);
                searchField.setDisable(false);
                searchBtn.setDisable(false);
                refreshBtn.setDisable(false);
                productTable.setDisable(false);
                refreshProducts();
            }
        });

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
        wrapper.getStyleClass().add("panel-surface");
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

        billingDiscountSpinner = new Spinner<>(0.0, 100.0, 0.0, 1.0);
        billingDiscountSpinner.setEditable(true);
        billingDiscountSpinner.setDisable(true);

        billingPaidField = new TextField();
        billingPaidField.setPromptText("Amount paid (optional)");
        billingPaidField.setDisable(true);

        billingPriceLabel = new Label("Unit Price: Rs. 0.00");
        billingCartCountLabel = new Label("Items in Cart: 0");
        billingSubtotalLabel = new Label("Subtotal: Rs. 0.00");
        billingDiscountLabel = new Label("Discount: Rs. 0.00 (0%)");
        billingTotalLabel = new Label("Payable: Rs. 0.00");
        billingTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        billingChangeLabel = new Label("Change: Rs. 0.00");

        Button addToCartBtn = new Button("Add to Cart");
        processSaleBtn = new Button("Process Sale");
        processSaleBtn.setDisable(true);
        Button removeSelectedBtn = new Button("Remove Selected");
        Button clearCartBtn = new Button("Clear Cart");
        Button exactPaidBtn = new Button("Set Paid = Payable");
        Button openSalesBtn = new Button("Open Sales History Tab");

        billingProductCombo.setOnAction(e -> updateBillingSelectionState());
        billingQtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateBillingSelectionState());
        billingDiscountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateBillingTotal());
        billingPaidField.textProperty().addListener((obs, oldVal, newVal) -> updateBillingTotal());

        addToCartBtn.setOnAction(e -> addSelectedProductToCart());
        processSaleBtn.setOnAction(e -> processSale());
        removeSelectedBtn.setOnAction(e -> removeSelectedCartItem());
        clearCartBtn.setOnAction(e -> clearBillingCart());
        exactPaidBtn.setOnAction(e -> {
            double subtotal = getCartSubtotal();
            double discountPercent = readDiscountPercent();
            double payable = Math.max(0.0, subtotal - (subtotal * discountPercent / 100.0));
            billingPaidField.setText(String.format("%.2f", payable));
        });
        openSalesBtn.setOnAction(e -> tabPane.getSelectionModel().select(salesTab));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Product"), 0, 0);
        grid.add(billingProductCombo, 1, 0);
        grid.add(new Label("Quantity"), 0, 1);
        grid.add(billingQtySpinner, 1, 1);
        grid.add(new Label("Discount (%)"), 0, 2);
        grid.add(billingDiscountSpinner, 1, 2);
        grid.add(new Label("Amount Paid"), 0, 3);
        grid.add(billingPaidField, 1, 3);

        billingCartTable = createBillingCartTable();
        billingCartTable.setItems(billingCartData);

        HBox cartActions = new HBox(10, addToCartBtn, removeSelectedBtn, clearCartBtn);
        HBox actions = new HBox(10, processSaleBtn, exactPaidBtn, openSalesBtn);

        VBox content = new VBox(15,
                new Label("Process Sale"),
                grid,
                billingPriceLabel,
                billingCartCountLabel,
                cartActions,
                billingCartTable,
                billingSubtotalLabel,
                billingDiscountLabel,
                billingTotalLabel,
                billingChangeLabel,
                actions
        );
        content.getStyleClass().add("panel-surface");
        content.setPadding(new Insets(20));
        content.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                refreshProducts();
                updateBillingSelectionState();
                updateBillingTotal();
            }
        });
        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createSalesTab() {
        Tab tab = new Tab("Sales History");
        tab.setClosable(false);

        TableView<Sale> salesTable = new TableView<>();
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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

        salesTable.getColumns().add(idCol);
        salesTable.getColumns().add(productCol);
        salesTable.getColumns().add(qtyCol);
        salesTable.getColumns().add(totalCol);
        salesTable.getColumns().add(dateCol);
        salesTable.setItems(salesData);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshSales());

        totalSalesLabel = new Label("Total Sales: Rs. 0.00");
        totalSalesLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox content = new VBox(10, refreshBtn, salesTable, totalSalesLabel);
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
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
        content.getStyleClass().add("panel-surface");
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);
        tab.setContent(content);
        return tab;
    }

    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(categoryCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(expiryCol);
        return table;
    }

    private void refreshProducts() {
        productData.setAll(productDAO.getAllProducts());
        syncBillingCartWithLatestProducts();

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
            updateBillingTotal();
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
            billingDiscountSpinner.setDisable(billingCartData.isEmpty());
            billingPaidField.setDisable(billingCartData.isEmpty());
            return;
        }

        int available = selected.getQuantity() - getCartQtyForProduct(selected.getProductId());
        billingPriceLabel.setText("Unit Price: " + formatCurrency(selected.getPrice()) + " (Available: " + Math.max(available, 0) + ")");

        if (available <= 0) {
            billingQtySpinner.setDisable(true);
            billingQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
        } else {
            billingQtySpinner.setDisable(false);
            int currentQty = billingQtySpinner.getValue() == null ? 1 : Math.max(1, billingQtySpinner.getValue());
            int initialQty = Math.min(currentQty, available);
            billingQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, available, initialQty));
        }
    }

    private void updateBillingTotal() {
        double subtotal = getCartSubtotal();
        if (billingCartCountLabel != null) {
            billingCartCountLabel.setText("Items in Cart: " + billingCartData.size());
        }

        if (subtotal <= 0) {
            billingSubtotalLabel.setText("Subtotal: Rs. 0.00");
            billingDiscountLabel.setText("Discount: Rs. 0.00 (0%)");
            billingTotalLabel.setText("Payable: Rs. 0.00");
            billingChangeLabel.setText("Change: Rs. 0.00");
            billingChangeLabel.setStyle("");
            billingDiscountSpinner.setDisable(true);
            billingPaidField.setDisable(true);
            processSaleBtn.setDisable(true);
            return;
        }

        billingDiscountSpinner.setDisable(false);
        billingPaidField.setDisable(false);

        double discountPercent = readDiscountPercent();
        double discountAmount = subtotal * discountPercent / 100.0;
        double payable = Math.max(0.0, subtotal - discountAmount);

        billingSubtotalLabel.setText("Subtotal: " + formatCurrency(subtotal));
        billingDiscountLabel.setText(
                "Discount: " + formatCurrency(discountAmount) + " (" + String.format("%.1f", discountPercent) + "%)"
        );
        billingTotalLabel.setText("Payable: " + formatCurrency(payable));

        boolean canProcess = true;
        Double paidAmount = parsePaidAmount();
        if (paidAmount == null) {
            billingChangeLabel.setText("Change: Rs. 0.00");
            billingChangeLabel.setStyle("");
        } else if (Double.isNaN(paidAmount) || paidAmount < 0) {
            billingChangeLabel.setText("Invalid paid amount");
            billingChangeLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            canProcess = false;
        } else if (paidAmount < payable) {
            billingChangeLabel.setText("Balance Due: " + formatCurrency(payable - paidAmount));
            billingChangeLabel.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
            canProcess = false;
        } else {
            billingChangeLabel.setText("Change: " + formatCurrency(paidAmount - payable));
            billingChangeLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        }

        processSaleBtn.setDisable(!canProcess);
    }

    private void processSale() {
        if (billingCartData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart Empty", "Add at least one product to cart.");
            return;
        }

        refreshProducts();
        for (BillingCartItem item : billingCartData) {
            Product product = productData.stream()
                    .filter(p -> p.getProductId() == item.getProductId())
                    .findFirst()
                    .orElse(null);
            if (product == null) {
                showAlert(Alert.AlertType.ERROR, "Product Missing", "Product not found: " + item.getProductName());
                return;
            }
            if (item.getQuantity() > product.getQuantity()) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Insufficient Stock",
                        "Not enough stock for " + item.getProductName()
                                + ". Requested: " + item.getQuantity()
                                + ", Available: " + product.getQuantity()
                );
                return;
            }
        }

        double subtotal = getCartSubtotal();
        if (subtotal <= 0) {
            showAlert(Alert.AlertType.WARNING, "Cart Empty", "Add valid products to cart.");
            return;
        }

        double discountPercent = readDiscountPercent();
        double discountAmount = subtotal * discountPercent / 100.0;
        double payable = Math.max(0.0, subtotal - discountAmount);

        Double paidAmount = parsePaidAmount();
        if (paidAmount != null) {
            if (Double.isNaN(paidAmount) || paidAmount < 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Paid Amount", "Enter a valid amount paid.");
                return;
            }
            if (paidAmount < payable) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Payment", "Paid amount is less than payable amount.");
                return;
            }
        }

        List<Sale> billLines = buildBillLinesWithDiscount(subtotal, payable);
        boolean success = saleService.processSaleBatch(billLines, user.getUserId());
        if (success) {
            String message = "Sale processed.\n"
                    + "Subtotal: " + formatCurrency(subtotal) + "\n"
                    + "Discount: " + formatCurrency(discountAmount) + " (" + String.format("%.1f", discountPercent) + "%)\n"
                    + "Payable: " + formatCurrency(payable);
            if (paidAmount != null) {
                message += "\nPaid: " + formatCurrency(paidAmount)
                        + "\nChange: " + formatCurrency(paidAmount - payable);
            }

            showAlert(Alert.AlertType.INFORMATION, "Sale Successful", message);
            clearBillingCart();
            billingDiscountSpinner.getValueFactory().setValue(0.0);
            billingPaidField.clear();
            refreshProducts();
            refreshSales();
        } else {
            showAlert(Alert.AlertType.ERROR, "Sale Failed", "Could not process sale.");
        }
    }

    private TableView<BillingCartItem> createBillingCartTable() {
        TableView<BillingCartItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setMinHeight(200);
        table.setPrefHeight(260);

        TableColumn<BillingCartItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getProductName()));

        TableColumn<BillingCartItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));

        TableColumn<BillingCartItem, Double> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUnitPrice()));
        unitPriceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        TableColumn<BillingCartItem, Double> totalCol = new TableColumn<>("Line Total");
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getLineTotal()));
        totalCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(unitPriceCol);
        table.getColumns().add(totalCol);
        return table;
    }

    private void addSelectedProductToCart() {
        Product selected = billingProductCombo.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Product", "Select a product to add.");
            return;
        }

        int qty = billingQtySpinner.getValue();
        if (qty <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than zero.");
            return;
        }

        int available = selected.getQuantity() - getCartQtyForProduct(selected.getProductId());
        if (qty > available) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Insufficient Stock",
                    "Only " + Math.max(available, 0) + " units are available for " + selected.getName() + "."
            );
            return;
        }

        BillingCartItem existing = billingCartData.stream()
                .filter(item -> item.getProductId() == selected.getProductId())
                .findFirst()
                .orElse(null);

        if (existing == null) {
            billingCartData.add(new BillingCartItem(
                    selected.getProductId(),
                    selected.getName(),
                    qty,
                    selected.getPrice()
            ));
        } else {
            existing.setQuantity(existing.getQuantity() + qty);
        }

        if (billingCartTable != null) {
            billingCartTable.refresh();
        }
        updateBillingSelectionState();
        updateBillingTotal();
    }

    private void removeSelectedCartItem() {
        if (billingCartData.isEmpty()) {
            return;
        }

        if (billingCartTable != null) {
            BillingCartItem selectedItem = billingCartTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                billingCartData.remove(selectedItem);
                if (billingCartTable != null) {
                    billingCartTable.refresh();
                }
                updateBillingSelectionState();
                updateBillingTotal();
                return;
            }
        }

        // Fallback: remove latest line.
        billingCartData.remove(billingCartData.size() - 1);
        if (billingCartTable != null) {
            billingCartTable.refresh();
        }
        updateBillingSelectionState();
        updateBillingTotal();
    }

    private void clearBillingCart() {
        billingCartData.clear();
        if (billingCartTable != null) {
            billingCartTable.refresh();
        }
        updateBillingSelectionState();
        updateBillingTotal();
    }

    private int getCartQtyForProduct(int productId) {
        return billingCartData.stream()
                .filter(item -> item.getProductId() == productId)
                .mapToInt(BillingCartItem::getQuantity)
                .sum();
    }

    private double getCartSubtotal() {
        return billingCartData.stream()
                .mapToDouble(BillingCartItem::getLineTotal)
                .sum();
    }

    private void syncBillingCartWithLatestProducts() {
        if (billingCartData.isEmpty()) {
            return;
        }

        List<BillingCartItem> validItems = new ArrayList<>();
        for (BillingCartItem item : billingCartData) {
            Product product = productData.stream()
                    .filter(p -> p.getProductId() == item.getProductId())
                    .findFirst()
                    .orElse(null);
            if (product == null || product.getQuantity() <= 0) {
                continue;
            }

            int clampedQty = Math.min(item.getQuantity(), product.getQuantity());
            if (clampedQty <= 0) {
                continue;
            }

            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(clampedQty);
            validItems.add(item);
        }

        billingCartData.setAll(validItems);
        if (billingCartTable != null) {
            billingCartTable.refresh();
        }
    }

    private List<Sale> buildBillLinesWithDiscount(double subtotal, double payable) {
        List<Sale> lines = new ArrayList<>();
        if (billingCartData.isEmpty() || subtotal <= 0) {
            return lines;
        }

        double runningTotal = 0.0;
        for (int i = 0; i < billingCartData.size(); i++) {
            BillingCartItem item = billingCartData.get(i);
            double linePayable;
            if (i == billingCartData.size() - 1) {
                linePayable = roundCurrency(payable - runningTotal);
            } else {
                linePayable = roundCurrency(item.getLineTotal() * payable / subtotal);
                runningTotal += linePayable;
            }
            if (linePayable < 0) {
                linePayable = 0;
            }

            Sale sale = new Sale();
            sale.setProductId(item.getProductId());
            sale.setQuantitySold(item.getQuantity());
            sale.setTotalPrice(linePayable);
            lines.add(sale);
        }

        return lines;
    }

    private double roundCurrency(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double readDiscountPercent() {
        if (billingDiscountSpinner == null || billingDiscountSpinner.getValue() == null) {
            return 0.0;
        }
        double value = billingDiscountSpinner.getValue();
        if (value < 0.0) {
            return 0.0;
        }
        return Math.min(value, 100.0);
    }

    private Double parsePaidAmount() {
        if (billingPaidField == null) {
            return null;
        }
        String text = billingPaidField.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private static class BillingCartItem {
        private final int productId;
        private String productName;
        private int quantity;
        private double unitPrice;

        private BillingCartItem(int productId, String productName, int quantity, double unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getLineTotal() {
            return unitPrice * quantity;
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
