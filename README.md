# SmartStock Offline

SmartStock Offline is a JavaFX desktop inventory and billing application for single-shop offline use.

## Tech Stack
- Java 17+
- JavaFX 17+
- MySQL 8+
- MySQL Connector/J 9.6.0

## Core Features
- Role-based login (Admin, Staff)
- Single-window tabbed dashboard
- Product management (add, update, delete, search)
- Billing and sales processing with stock updates
- Sales history and total sales summary
- Reports (all products, low stock, expiring soon, by category)
- Alerts (low stock, expiry)
- Dark mode toggle
- Fullscreen/maximized login and dashboard windows
- Currency display in rupees (Rs.)

## Tab Behavior
- Admin tabs: Products, Sales History, Reports, Alerts, Account
- Staff tabs: Products, Account, Billing, Sales History, Alert, Repport

## Project Structure
```
SmartOfflineInventorySystem/
  src/com/inventory/
    dao/
    model/
    service/
    ui/
    util/
  resources/icons/
  lib/mysql-connector-j-9.6.0.jar
  database_setup.sql
  style.css
  run.bat
```

## Prerequisites
1. Install JDK 17 or newer.
2. Install JavaFX SDK and keep `lib` path ready.
3. Install and run MySQL Server 8+.

## Database Setup
Run `database_setup.sql` in MySQL Workbench or MySQL CLI.

Example (MySQL CLI):
```sql
source C:/MANVITH/trail/SmartOfflineInventorySystem/database_setup.sql;
```

This creates:
- Database: `inventory_db`
- Tables: `users`, `products`, `sales`, `stock_history`
- Default users and sample products

## Database Configuration
The app reads DB settings from environment variables first:
- `INVENTORY_DB_URL`
- `INVENTORY_DB_USER`
- `INVENTORY_DB_PASSWORD`

Default fallback values are defined in `src/com/inventory/util/DBConnection.java`.

## Run (Recommended)
Use the launcher:
```bat
run.bat
```

`run.bat` will:
- Validate `java` and `javac`
- Detect JavaFX path from `JAVAFX_LIB`, `C:\javafx\lib`, or `./javafx/lib`
- Compile all Java files
- Launch `com.inventory.ui.FXLoginApp`

If JavaFX is elsewhere:
```bat
set JAVAFX_LIB=D:\javafx-sdk-21\lib
run.bat
```

## VS Code Tasks (Optional)
- Build task: `Build JavaFX App`
- Run task: `Run JavaFX App`

## Default Login
- Admin: `admin` / `admin123`
- Staff: `staff` / `staff123`

## Troubleshooting
1. JavaFX error: set `JAVAFX_LIB` correctly and ensure `javafx.controls.jar` exists.
2. MySQL connection error: confirm MySQL service is running and DB credentials are correct.
3. JDBC driver error: verify `lib/mysql-connector-j-9.6.0.jar` exists.
4. Compile failures: ensure JDK 17+ is installed and on PATH.

## Notes
- This is an offline desktop app.
- Internet is not required to run after dependencies are installed.
