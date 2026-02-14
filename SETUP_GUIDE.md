# SmartStock Offline - Setup Guide

## 1. Install Required Software

### 1.1 Java (JDK 17+)
- Install JDK 17 or newer.
- Verify:
```bat
java -version
javac -version
```

### 1.2 JavaFX SDK
- Download JavaFX SDK.
- Keep JavaFX `lib` folder path ready.
- Preferred location: `C:\javafx\lib`

### 1.3 MySQL Server (8+)
- Install MySQL Server.
- Start MySQL service.

## 2. Setup Database

### Option A: MySQL Command Line
```sql
source C:/MANVITH/trail/SmartOfflineInventorySystem/database_setup.sql;
```

### Option B: MySQL Workbench
1. Open Workbench.
2. Open `database_setup.sql`.
3. Execute the script.

## 3. Configure Database Connection (Optional)
App supports environment variables:
- `INVENTORY_DB_URL`
- `INVENTORY_DB_USER`
- `INVENTORY_DB_PASSWORD`

If not set, defaults in `src/com/inventory/util/DBConnection.java` are used.

Example (PowerShell):
```powershell
$env:INVENTORY_DB_URL="jdbc:mysql://localhost:3306/inventory_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:INVENTORY_DB_USER="inventory_user"
$env:INVENTORY_DB_PASSWORD="inventory123"
```

## 4. Run the Application

### Recommended: `run.bat`
From project root:
```bat
run.bat
```

If JavaFX is not at `C:\javafx\lib`:
```bat
set JAVAFX_LIB=D:\javafx-sdk-21\lib
run.bat
```

## 5. Login Credentials
- Admin: `admin` / `admin123`
- Staff: `staff` / `staff123`

## 6. Verify Main Flows
1. Login screen opens centered and maximized.
2. Dashboard opens in single window with tabs.
3. Product search/add/update/delete works (Admin).
4. Billing updates stock and sales history.
5. Reports and alerts load correctly.
6. Logout returns to login page.

## 7. Common Problems
1. `java` not found:
   - Install JDK and add to PATH.
2. JavaFX module errors:
   - Set `JAVAFX_LIB` to correct `lib` folder.
3. MySQL connection failed:
   - Start MySQL service.
   - Re-check DB user/password and DB name.
4. JDBC driver not found:
   - Ensure `lib/mysql-connector-j-9.6.0.jar` exists.

## 8. Offline Usage
- No internet is required for normal use.
- App and MySQL run locally on the same PC.
