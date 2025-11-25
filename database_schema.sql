-- ============================================
-- COMPLETE INVENTORY MANAGEMENT SYSTEM
-- Database Schema with Enhanced Features
-- ============================================

-- Drop existing database if exists (CAUTION: This will delete all data)
DROP DATABASE IF EXISTS inventory_management;

-- Create database
CREATE DATABASE inventory_management;
USE inventory_management;

-- ============================================
-- TABLE 1: User (with roles and permissions)
-- ============================================
CREATE TABLE User (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed password
    role ENUM('ADMIN', 'MANAGER', 'USER') DEFAULT 'USER',
    full_name VARCHAR(100),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- ============================================
-- TABLE 2: Category
-- ============================================
CREATE TABLE Category (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category_name (name)
);

-- ============================================
-- TABLE 3: Supplier
-- ============================================
CREATE TABLE Supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_supplier_name (name)
);

-- ============================================
-- TABLE 4: Product
-- ============================================
CREATE TABLE Product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL,
    cost_price DECIMAL(10, 2),  -- For profit calculation
    reorder_level INT DEFAULT 20,  -- Low stock threshold
    category_id INT NOT NULL,
    supplier_id INT NOT NULL,
    barcode VARCHAR(50),
    sku VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Category(category_id) ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id) ON DELETE RESTRICT,
    INDEX idx_product_name (name),
    INDEX idx_barcode (barcode),
    INDEX idx_stock_level (quantity_in_stock),
    CHECK (quantity_in_stock >= 0),
    CHECK (price >= 0)
);

-- ============================================
-- TABLE 5: Sale
-- ============================================
CREATE TABLE Sale (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,  -- Price at time of sale
    total_amount DECIMAL(10, 2) NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,  -- Who made the sale
    payment_method ENUM('CASH', 'CARD', 'MOBILE', 'OTHER') DEFAULT 'CASH',
    notes TEXT,
    FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE SET NULL,
    INDEX idx_sale_date (sale_date),
    INDEX idx_product_sale (product_id),
    CHECK (quantity_sold > 0),
    CHECK (unit_price >= 0)
);

-- ============================================
-- TABLE 6: Purchase Orders (New)
-- ============================================
CREATE TABLE PurchaseOrder (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery DATE,
    status ENUM('PENDING', 'RECEIVED', 'CANCELLED') DEFAULT 'PENDING',
    received_date TIMESTAMP NULL,
    user_id INT,  -- Who created the order
    notes TEXT,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE SET NULL,
    INDEX idx_order_date (order_date),
    INDEX idx_status (status)
);

-- ============================================
-- TABLE 7: Audit Log (Activity Tracking)
-- ============================================
CREATE TABLE AuditLog (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    table_name VARCHAR(50),
    record_id INT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action),
    INDEX idx_timestamp (timestamp)
);

-- ============================================
-- TABLE 8: System Settings (New)
-- ============================================
CREATE TABLE SystemSettings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT,
    FOREIGN KEY (updated_by) REFERENCES User(user_id) ON DELETE SET NULL
);

-- ============================================
-- INSERT SAMPLE DATA
-- ============================================

-- Sample Users (password is 'admin123' hashed with BCrypt)
-- Note: These are BCrypt hashes for 'admin123'
INSERT INTO User (username, password, role, full_name, email) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'Admin User', 'admin@inventory.com'),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER', 'Manager User', 'manager@inventory.com'),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'Regular User', 'user@inventory.com');

-- Sample Categories
INSERT INTO Category (name, description) VALUES
('Electronics', 'Electronic devices and gadgets'),
('Clothing', 'Apparel and fashion items'),
('Food & Beverages', 'Food products and drinks'),
('Books', 'Books and publications'),
('Furniture', 'Home and office furniture'),
('Sports Equipment', 'Sports and fitness gear'),
('Toys & Games', 'Children toys and games'),
('Beauty & Health', 'Cosmetics and health products');

-- Sample Suppliers
INSERT INTO Supplier (name, contact_person, email, phone, address) VALUES
('Tech Solutions Ltd', 'John Doe', 'john@techsolutions.com', '+1-555-0101', '123 Tech Street, Silicon Valley'),
('Fashion World Inc', 'Jane Smith', 'jane@fashionworld.com', '+1-555-0102', '456 Fashion Ave, New York'),
('Food Distributors Co', 'Bob Johnson', 'bob@fooddist.com', '+1-555-0103', '789 Food Lane, Chicago'),
('Book Publishers Ltd', 'Alice Brown', 'alice@bookpub.com', '+1-555-0104', '321 Book Road, Boston'),
('Furniture Makers Inc', 'Charlie Davis', 'charlie@furnituremake.com', '+1-555-0105', '654 Furniture Blvd, Seattle'),
('Sports Gear Co', 'Diana Wilson', 'diana@sportsgear.com', '+1-555-0106', '987 Sports Way, Denver');

-- Sample Products
INSERT INTO Product (name, description, quantity_in_stock, price, cost_price, reorder_level, category_id, supplier_id, barcode, sku) VALUES
('Laptop Dell XPS 15', '15-inch premium laptop', 45, 1299.99, 1000.00, 10, 1, 1, '1234567890123', 'DELL-XPS-15'),
('iPhone 13 Pro', 'Apple smartphone 128GB', 30, 999.99, 800.00, 15, 1, 1, '1234567890124', 'APPL-IP13-PRO'),
('Samsung 4K TV 55"', '55-inch smart TV', 18, 699.99, 550.00, 5, 1, 1, '1234567890125', 'SAMS-TV-55'),
('Mens T-Shirt Blue', 'Cotton t-shirt size M', 150, 19.99, 10.00, 50, 2, 2, '1234567890126', 'TSH-BLUE-M'),
('Womens Jeans Black', 'Denim jeans size 8', 80, 49.99, 25.00, 30, 2, 2, '1234567890127', 'JEAN-BLK-8'),
('Coffee Beans 1kg', 'Premium arabica beans', 200, 15.99, 8.00, 100, 3, 3, '1234567890128', 'COFF-ARAB-1KG'),
('Energy Drink Pack', 'Pack of 24 cans', 120, 29.99, 18.00, 50, 3, 3, '1234567890129', 'ENRG-24PK'),
('Fiction Novel', 'Bestseller hardcover', 60, 24.99, 12.00, 20, 4, 4, '1234567890130', 'BOOK-FICT-HC'),
('Office Desk Oak', 'Solid oak desk 120cm', 12, 299.99, 180.00, 5, 5, 5, '1234567890131', 'DESK-OAK-120'),
('Office Chair Ergonomic', 'Adjustable mesh chair', 25, 199.99, 120.00, 10, 5, 5, '1234567890132', 'CHAIR-ERGO'),
('Yoga Mat Premium', 'Non-slip exercise mat', 75, 39.99, 20.00, 30, 6, 6, '1234567890133', 'YOGA-MAT-PREM'),
('Basketball Wilson', 'Official size basketball', 40, 29.99, 15.00, 20, 6, 6, '1234567890134', 'BBAL-WILS'),
('LEGO City Set', '500 pieces construction', 90, 59.99, 35.00, 40, 7, 1, '1234567890135', 'LEGO-CITY-500'),
('Board Game Monopoly', 'Classic family game', 55, 34.99, 18.00, 25, 7, 1, '1234567890136', 'GAME-MONO'),
('Face Cream Anti-Aging', 'Premium skincare 50ml', 110, 44.99, 22.00, 50, 8, 2, '1234567890137', 'FACE-ANTIAGE');

-- Sample Sales
INSERT INTO Sale (product_id, quantity_sold, unit_price, total_amount, user_id, payment_method) VALUES
(1, 2, 1299.99, 2599.98, 1, 'CARD'),
(2, 1, 999.99, 999.99, 2, 'CARD'),
(4, 5, 19.99, 99.95, 3, 'CASH'),
(6, 3, 15.99, 47.97, 2, 'CASH'),
(11, 2, 39.99, 79.98, 1, 'MOBILE');

-- Update product stock after sales
UPDATE Product SET quantity_in_stock = quantity_in_stock - 2 WHERE product_id = 1;
UPDATE Product SET quantity_in_stock = quantity_in_stock - 1 WHERE product_id = 2;
UPDATE Product SET quantity_in_stock = quantity_in_stock - 5 WHERE product_id = 4;
UPDATE Product SET quantity_in_stock = quantity_in_stock - 3 WHERE product_id = 6;
UPDATE Product SET quantity_in_stock = quantity_in_stock - 2 WHERE product_id = 11;

-- Sample System Settings
INSERT INTO SystemSettings (setting_key, setting_value, description) VALUES
('LOW_STOCK_THRESHOLD', '20', 'Default low stock alert threshold'),
('CURRENCY_SYMBOL', '$', 'Currency symbol for display'),
('TAX_RATE', '0.10', 'Default tax rate (10%)'),
('COMPANY_NAME', 'My Inventory Store', 'Company name for reports'),
('THEME', 'LIGHT', 'Default theme (LIGHT/DARK)'),
('BACKUP_ENABLED', 'true', 'Enable automatic backups'),
('SESSION_TIMEOUT', '30', 'Session timeout in minutes');

-- ============================================
-- USEFUL VIEWS FOR REPORTS
-- ============================================

-- View: Low Stock Products
CREATE VIEW view_low_stock_products AS
SELECT
    p.product_id,
    p.name,
    p.quantity_in_stock,
    p.reorder_level,
    c.name AS category_name,
    s.name AS supplier_name
FROM Product p
JOIN Category c ON p.category_id = c.category_id
JOIN Supplier s ON p.supplier_id = s.supplier_id
WHERE p.quantity_in_stock < p.reorder_level
ORDER BY p.quantity_in_stock ASC;

-- View: Product Inventory Value
CREATE VIEW view_inventory_value AS
SELECT
    p.product_id,
    p.name,
    p.quantity_in_stock,
    p.price,
    (p.quantity_in_stock * p.price) AS stock_value,
    c.name AS category_name
FROM Product p
JOIN Category c ON p.category_id = c.category_id
ORDER BY stock_value DESC;

-- View: Sales Summary
CREATE VIEW view_sales_summary AS
SELECT
    s.sale_id,
    p.name AS product_name,
    s.quantity_sold,
    s.unit_price,
    s.total_amount,
    s.sale_date,
    u.username AS sold_by,
    s.payment_method
FROM Sale s
JOIN Product p ON s.product_id = p.product_id
LEFT JOIN User u ON s.user_id = u.user_id
ORDER BY s.sale_date DESC;

-- ============================================
-- STORED PROCEDURES
-- ============================================

-- Procedure: Record Sale with Stock Update
DELIMITER //
CREATE PROCEDURE sp_record_sale(
    IN p_product_id INT,
    IN p_quantity INT,
    IN p_user_id INT,
    IN p_payment_method VARCHAR(20)
)
BEGIN
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_stock INT;

    -- Get current price and stock
    SELECT price, quantity_in_stock INTO v_price, v_stock
    FROM Product WHERE product_id = p_product_id;

    -- Check stock availability
    IF v_stock < p_quantity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;

    -- Start transaction
    START TRANSACTION;

    -- Insert sale
    INSERT INTO Sale (product_id, quantity_sold, unit_price, total_amount, user_id, payment_method)
    VALUES (p_product_id, p_quantity, v_price, v_price * p_quantity, p_user_id, p_payment_method);

    -- Update stock
    UPDATE Product
    SET quantity_in_stock = quantity_in_stock - p_quantity
    WHERE product_id = p_product_id;

    COMMIT;
END //
DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger: Log product updates
DELIMITER //
CREATE TRIGGER trg_product_update_log
AFTER UPDATE ON Product
FOR EACH ROW
BEGIN
    INSERT INTO AuditLog (user_id, action, table_name, record_id, old_value, new_value)
    VALUES (
        NULL,
        'UPDATE',
        'Product',
        NEW.product_id,
        CONCAT('Stock: ', OLD.quantity_in_stock, ', Price: ', OLD.price),
        CONCAT('Stock: ', NEW.quantity_in_stock, ', Price: ', NEW.price)
    );
END //
DELIMITER ;

-- ============================================
-- SAMPLE QUERIES FOR REPORTS
-- ============================================

-- 1. Total Sales Revenue
-- SELECT SUM(total_amount) AS total_revenue FROM Sale;

-- 2. Sales by Date Range
-- SELECT * FROM Sale WHERE sale_date BETWEEN '2024-01-01' AND '2024-12-31';

-- 3. Top Selling Products
-- SELECT p.name, SUM(s.quantity_sold) AS total_sold, SUM(s.total_amount) AS revenue
-- FROM Sale s JOIN Product p ON s.product_id = p.product_id
-- GROUP BY p.product_id ORDER BY total_sold DESC LIMIT 10;

-- 4. Low Stock Alert
-- SELECT * FROM view_low_stock_products;

-- 5. Inventory Valuation
-- SELECT SUM(stock_value) AS total_inventory_value FROM view_inventory_value;

-- 6. Sales by Category
-- SELECT c.name, COUNT(s.sale_id) AS sales_count, SUM(s.total_amount) AS revenue
-- FROM Sale s
-- JOIN Product p ON s.product_id = p.product_id
-- JOIN Category c ON p.category_id = c.category_id
-- GROUP BY c.category_id;

-- ============================================
-- END OF SCHEMA
-- ============================================
