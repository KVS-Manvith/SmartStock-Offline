-- ================================================
-- Smart Offline Inventory System - Database Setup
-- ================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- ================================================
-- Table: users
-- ================================================
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- Table: products
-- ================================================
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ================================================
-- Table: sales
-- ================================================
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ================================================
-- Table: stock_history
-- ================================================
CREATE TABLE IF NOT EXISTS stock_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'ADD', 'UPDATE', 'SALE', 'DELETE'
    quantity_change INT NOT NULL,
    previous_quantity INT,
    new_quantity INT,
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ================================================
-- Insert Sample Data
-- ================================================

-- Insert Admin and Staff users (idempotent)
INSERT INTO users (username, password, role)
SELECT * FROM (
    SELECT 'admin' AS username, 'admin123' AS password, 'ADMIN' AS role
) AS user_seed
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

INSERT INTO users (username, password, role)
SELECT * FROM (
    SELECT 'staff' AS username, 'staff123' AS password, 'STAFF' AS role
) AS user_seed
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'staff'
);

-- Insert sample products (idempotent by name+category)
INSERT INTO products (name, category, quantity, price, expiry_date)
SELECT seed.name, seed.category, seed.quantity, seed.price, seed.expiry_date
FROM (
    SELECT 'Milk' AS name, 'Dairy' AS category, 50 AS quantity, 2.50 AS price, DATE('2026-02-25') AS expiry_date
    UNION ALL SELECT 'Bread', 'Bakery', 100, 1.50, DATE('2026-02-20')
    UNION ALL SELECT 'Eggs', 'Dairy', 200, 3.00, DATE('2026-03-01')
    UNION ALL SELECT 'Cheese', 'Dairy', 30, 5.00, DATE('2026-03-15')
    UNION ALL SELECT 'Chicken', 'Meat', 40, 8.00, DATE('2026-02-18')
    UNION ALL SELECT 'Rice', 'Grains', 150, 4.50, DATE('2027-01-01')
    UNION ALL SELECT 'Pasta', 'Grains', 80, 2.00, DATE('2026-12-31')
    UNION ALL SELECT 'Orange Juice', 'Beverages', 60, 3.50, DATE('2026-02-22')
    UNION ALL SELECT 'Yogurt', 'Dairy', 45, 2.75, DATE('2026-02-19')
    UNION ALL SELECT 'Butter', 'Dairy', 25, 4.25, DATE('2026-03-10')
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.name = seed.name
      AND p.category = seed.category
);

-- ================================================
-- Create User and Grant Permissions
-- ================================================
-- Create app user (idempotent)
CREATE USER IF NOT EXISTS 'inventory_user'@'localhost' IDENTIFIED BY 'inventory123';
ALTER USER 'inventory_user'@'localhost' IDENTIFIED BY 'inventory123';

-- Grant privileges
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'localhost';
FLUSH PRIVILEGES;

-- ================================================
-- Useful Queries for Testing
-- ================================================

-- View all users
-- SELECT * FROM users;

-- View all products
-- SELECT * FROM products;

-- View low stock products (less than 50)
-- SELECT * FROM products WHERE quantity < 50;

-- View products expiring soon (within 7 days)
-- SELECT * FROM products WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY);

-- View sales history
-- SELECT s.*, p.name, u.username FROM sales s
-- JOIN products p ON s.product_id = p.product_id
-- JOIN users u ON s.user_id = u.user_id
-- ORDER BY s.sale_date DESC;
