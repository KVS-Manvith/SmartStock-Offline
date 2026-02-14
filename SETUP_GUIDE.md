# Quick Setup Guide

## Step 1: Install Prerequisites

### 1.1 Install Java JDK 17+
1. Download from: https://www.oracle.com/java/technologies/downloads/
2. Run the installer
3. Verify: Open Command Prompt and type: `java -version`

### 1.2 Install JavaFX SDK
1. Download from: https://gluonhq.com/products/javafx/
2. Extract to: `C:\javafx\`
3. Your structure should be: `C:\javafx\lib\` (containing .jar files)

### 1.3 Install MySQL
1. Download from: https://dev.mysql.com/downloads/mysql/
2. Run installer and select "Developer Default"
3. During setup, set root password (remember this!)
4. Start MySQL service

## Step 2: Setup Database

### Option A: Using MySQL Command Line
```bash
# Open MySQL Command Line Client
# Enter your root password

# Run the setup script
source C:\MANVITH\trail\SmartOfflineInventorySystem\database_setup.sql;
```

### Option B: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to local instance
3. Go to File → Open SQL Script
4. Select `database_setup.sql` from project folder
5. Click Execute (lightning bolt icon)

## Step 3: Verify Database Setup

```sql
USE inventory_db;
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM products;
```

You should see:
- 4 tables created (users, products, sales, stock_history)
- 2 users (admin, staff)
- 10 sample products

## Step 4: Configure Project (If Needed)

Only if you changed MySQL settings:

Edit `src\com\inventory\util\DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
private static final String USER = "inventory_user";     // Change if needed
private static final String PASSWORD = "inventory123";    // Change if needed
```

Or set environment variables:
- `INVENTORY_DB_URL`
- `INVENTORY_DB_USER`
- `INVENTORY_DB_PASSWORD`

## Step 5: Run the Application

### Using VS Code (Recommended):

1. Open project folder in VS Code
2. Press **Ctrl+Shift+P**
3. Type and select: "Tasks: Run Build Task"
4. Wait for compilation to complete
5. Press **Ctrl+Shift+P** again
6. Type and select: "Tasks: Run Task"
7. Select: "Run JavaFX App"

### Using Command Line:

```bash
# Navigate to project directory
cd C:\MANVITH\trail\SmartOfflineInventorySystem

# Compile
javac --module-path C:/javafx/lib --add-modules javafx.controls,javafx.fxml -cp lib/mysql-connector-j-9.6.0.jar -d out src/com/inventory/*/*.java

# Run
java --module-path C:/javafx/lib --add-modules javafx.controls,javafx.fxml -cp "out;lib/mysql-connector-j-9.6.0.jar" com.inventory.ui.FXLoginApp
```

## Step 6: Login

Use these credentials:
- **Admin Access:**
  - Username: `admin`
  - Password: `admin123`

- **Staff Access:**
  - Username: `staff`
  - Password: `staff123`

## Common Issues & Solutions

### Issue 1: "javafx.* package does not exist"
**Solution:** 
- Verify JavaFX is installed at `C:\javafx\lib\`
- Update path in tasks if installed elsewhere

### Issue 2: "Cannot connect to MySQL"
**Solution:**
- Ensure MySQL service is running:
  - Windows: Services → MySQL → Start
- Check username/password in DBConnection.java
- Run database_setup.sql again

### Issue 3: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
**Solution:**
- Verify `mysql-connector-j-9.6.0.jar` is in `lib/` folder
- Check classpath includes the jar file

### Issue 4: Compilation errors
**Solution:**
- Ensure JDK 17+ is installed: `java -version`
- Set JAVA_HOME environment variable
- Verify all source files are present

## What to Try First

1. **View Products:**
   - Login as admin
   - Click "View Products"
   - You should see 10 sample products

2. **Add a Product:**
   - Click "Add Product" from Admin Dashboard
   - Fill in details and click "Add Product"

3. **Process a Sale:**
   - Click "Billing / Sales"
   - Select a product
   - Enter quantity
   - Click "Process Sale"

4. **Check Alerts:**
   - Click "Alerts" from Admin Dashboard
   - View low stock items
   - View expiring products

5. **Generate Reports:**
   - Click "Reports"
   - Explore different tabs
   - Try adjusting filters

## Next Steps

- Change default passwords for security
- Add your own products
- Explore all features
- Customize as needed

## Need Help?

Check the main README.md for detailed documentation and troubleshooting.

---

**Congratulations! 🎉** Your inventory system is ready to use!
