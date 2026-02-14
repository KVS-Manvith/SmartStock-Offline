# Smart Offline Inventory System

![Java](https://img.shields.io/badge/Java-17+-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange)

A comprehensive JavaFX-based desktop application for managing inventory with features like product management, sales tracking, reporting, and automated alerts.

## 📋 Features

### User Management
- **Role-based Access Control**: Admin and Staff roles with different permissions
- **Secure Login**: Username and password authentication

### Product Management
- ✅ Add, Edit, and Delete products
- ✅ Track product details: Name, Category, Quantity, Price, Expiry Date
- ✅ Search products by name or category
- ✅ Real-time stock updates

### Sales & Billing
- 💰 Process sales transactions
- 💰 Automatic stock deduction
- 💰 Sales history with date/time tracking
- 💰 Total sales reporting

### Reports
- 📊 All Products Report
- 📊 Low Stock Report (configurable threshold)
- 📊 Products Expiring Soon (configurable days)
- 📊 Category-wise Report

### Alerts
- 🔔 Low Stock Alerts (products with quantity ≤ 10)
- 🔔 Expiry Alerts (products expiring within 7 days)
- 🔔 Visual indicators with color coding

### User Interface
- 🎨 Modern and intuitive JavaFX UI
- 🎨 Separate dashboards for Admin and Staff
- 🎨 Keyboard shortcuts for common actions
- 🎨 Responsive table views with sorting

## 🛠️ Technology Stack

- **Frontend**: JavaFX 17+
- **Backend**: Java 17+
- **Database**: MySQL 8.0+
- **JDBC Driver**: MySQL Connector/J 9.6.0

## 📁 Project Structure

```
SmartOfflineInventorySystem/
├── src/
│   └── com/
│       └── inventory/
│           ├── dao/              # Data Access Objects
│           │   ├── ProductDAO.java
│           │   ├── SaleDAO.java
│           │   └── UserDAO.java
│           ├── model/            # Entity Classes
│           │   ├── Product.java
│           │   ├── Sale.java
│           │   └── User.java
│           ├── service/          # Business Logic
│           │   ├── AlertService.java
│           │   ├── ExpiryAlertService.java
│           │   ├── ProductService.java
│           │   ├── ReportService.java
│           │   ├── SaleService.java
│           │   └── UserService.java
│           ├── ui/               # User Interface
│           │   ├── AddProductUI.java
│           │   ├── AdminDashboard.java
│           │   ├── AlertUI.java
│           │   ├── BillingUI.java
│           │   ├── DashboardUI.java
│           │   ├── FXLoginApp.java
│           │   ├── ProductUI.java
│           │   ├── ReportUI.java
│           │   ├── SalesHistoryUI.java
│           │   └── StaffDashboard.java
│           └── util/             # Utilities
│               └── DBConnection.java
├── lib/                          # External Libraries
│   └── mysql-connector-j-9.6.0.jar
├── resources/
│   ├── icons/                    # UI Icons
│   └── bills/                    # Generated Bills
├── database_setup.sql            # Database Setup Script
├── style.css                     # JavaFX Stylesheet
└── README.md

```

## 🚀 Getting Started

### Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **JavaFX SDK 17 or higher**
   - Download from: https://gluonhq.com/products/javafx/
   - Extract to: `C:\javafx\` (or update paths in tasks.json)

3. **MySQL Server 8.0+**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Install and start MySQL service

4. **MySQL Connector/J** (Included in `lib/` folder)

### Database Setup

1. Open MySQL Command Line or MySQL Workbench

2. Run the database setup script:
   ```sql
   source database_setup.sql
   ```
   
   Or manually execute the contents of `database_setup.sql`

3. The script will:
   - Create `inventory_db` database
   - Create necessary tables (users, products, sales, stock_history)
   - Insert sample data
   - Create user `inventory_user` with password `inventory123`

### Configuration

Update database credentials if needed in:
`src/com/inventory/util/DBConnection.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
private static final String USER = "inventory_user";
private static final String PASSWORD = "inventory123";
```

Or set environment variables:
- `INVENTORY_DB_URL`
- `INVENTORY_DB_USER`
- `INVENTORY_DB_PASSWORD`

### Running the Application

#### Option 1: Using VS Code Tasks

1. Open the project in VS Code
2. Press `Ctrl+Shift+P` and select "Tasks: Run Task"
3. Select "Build JavaFX App" to compile
4. Select "Run JavaFX App" to run

#### Option 2: Command Line

**Compile:**
```bash
javac --module-path C:/javafx/lib --add-modules javafx.controls,javafx.fxml -cp lib/mysql-connector-j-9.6.0.jar -d out src/com/inventory/*/*.java
```

**Run:**
```bash
java --module-path C:/javafx/lib --add-modules javafx.controls,javafx.fxml -cp "out;lib/mysql-connector-j-9.6.0.jar" com.inventory.ui.FXLoginApp
```

## 🔐 Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Admin |
| staff | staff123 | Staff |

## 📖 User Guide

### Admin Features

1. **Product Management**
   - Add new products with details
   - Edit existing products
   - Delete products
   - Search and filter products

2. **Billing & Sales**
   - Process sales transactions
   - View sales history
   - Track total sales amount

3. **Reports**
   - View all products
   - Monitor low stock items
   - Check products expiring soon
   - Generate category-wise reports

4. **Alerts**
   - View low stock alerts
   - Check expiry alerts

### Staff Features

- View all products
- Search products
- Limited to read-only access

## 🎨 Screenshots

### Login Screen
- User authentication with role-based access

### Admin Dashboard
- Complete inventory management
- Sales and billing
- Reports and alerts

### Product Management
- CRUD operations on products
- Search and filter capabilities

### Billing System
- Product selection
- Quantity management
- Automatic total calculation
- Stock updates

### Reports
- Multiple report types
- Tabbed interface
- Configurable parameters

## 🔧 Troubleshooting

### Common Issues

1. **JavaFX not found**
   - Ensure JavaFX SDK is downloaded and path is correct
   - Update `--module-path` in tasks.json

2. **MySQL Connection Failed**
   - Verify MySQL service is running
   - Check credentials in DBConnection.java
   - Ensure database is created

3. **Compilation Errors**
   - Verify JDK 17+ is installed
   - Check classpath includes MySQL connector

## 📝 Database Schema

### Users Table
```sql
users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  role ENUM('ADMIN', 'STAFF') NOT NULL
)
```

### Products Table
```sql
products (
  product_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  expiry_date DATE
)
```

### Sales Table
```sql
sales (
  sale_id INT PRIMARY KEY AUTO_INCREMENT,
  product_id INT NOT NULL,
  quantity_sold INT NOT NULL,
  total_price DECIMAL(10, 2) NOT NULL,
  sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  user_id INT,
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
)
```

## 🚧 Future Enhancements

- [ ] Barcode scanning integration
- [ ] PDF report generation
- [ ] Email alerts for low stock
- [ ] Multi-user concurrent access
- [ ] Backup and restore functionality
- [ ] Dashboard with charts and graphs
- [ ] Supplier management
- [ ] Purchase order tracking

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Developer

Developed with ❤️ using Java and JavaFX

## 📞 Support

For issues or questions, please create an issue in the repository.

---

**Note**: This is a desktop application designed for offline use. All data is stored locally in MySQL database.
