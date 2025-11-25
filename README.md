# Complete Inventory Management System

A comprehensive JavaFX-based inventory management system with advanced features including user authentication, role-based access control, sales tracking, reporting, and analytics.

## Features Overview

### ✅ IMPLEMENTED FEATURES

#### 1. Database Layer
- **MySQL Database** with normalized tables
- **5 Core Tables**: User, Category, Supplier, Product, Sale
- **3 Additional Tables**: PurchaseOrder, AuditLog, SystemSettings
- Foreign key relationships and constraints
- Indexes for performance optimization
- Database views for reporting
- Stored procedures and triggers

#### 2. Model Classes (POJOs)
- User (with role-based access)
- Category
- Supplier
- Product
- Sale
- All models include validation and utility methods

#### 3. DAO Classes (Data Access Layer)
- **UserDAO**: Authentication, CRUD, BCrypt password hashing
- **CategoryDAO**: CRUD + Search
- **SupplierDAO**: CRUD + Search
- **ProductDAO**: CRUD + Search + Low Stock alerts
- **SaleDAO**: CRUD + Transactions + Revenue calculations
- **ReportsDAO**: Comprehensive analytics and reporting

#### 4. Security Features
- **BCrypt Password Hashing** - Secure password storage
- **Role-Based Access Control**:
  - ADMIN: Full system access, user management
  - MANAGER: Inventory management, reports
  - USER: Basic operations
- **Session Management** - Track last login
- **Active/Inactive Users** - Account status management
- **SQL Injection Protection** - PreparedStatements throughout

#### 5. User Interface
- **Login View** - Secure authentication with animations
- **Dashboard View** - Real-time statistics and navigation
- **Products Management** - Full CRUD with search
- **Categories Management** - Full CRUD with search
- **Suppliers Management** - Full CRUD with search
- **Sales Management** - Transaction processing
- **User Management** - Admin-only user administration
- **Reports & Analytics** - Comprehensive reporting module

#### 6. Reports & Analytics
- Total Revenue Tracking
- Sales by Date Range
- Top Selling Products
- Sales by Category
- Inventory Valuation
- Low Stock Alerts
- Monthly/Daily Sales Summaries
- Profit Analysis

#### 7. Data Export/Import
- **CSV Export**: Products, Categories, Suppliers, Sales, Users
- **CSV Import**: Bulk data import capabilities
- File chooser integration

#### 8. UI/UX Features
- Modern, professional design
- Purple gradient theme
- Color-coded feedback (green/red/blue)
- Icon-based navigation
- Responsive layouts
- Card-based design
- Shadow effects and rounded corners
- Hover effects
- Status messages
- Confirmation dialogs
- Loading indicators

## Installation & Setup

### Prerequisites
- Java 9 or higher
- MySQL Server 5.7+
- Maven 3.6+

### Step 1: Database Setup
```bash
# Start MySQL server
mysql -u root -p

# Run the database schema
mysql -u root -p < database_schema.sql
```

**Default Test Credentials:**
- Username: `admin` / Password: `admin123` (ADMIN role)
- Username: `manager` / Password: `admin123` (MANAGER role)
- Username: `user` / Password: `admin123` (USER role)

### Step 2: Configure Database Connection

Edit `src/main/java/com/example/dummy_inventory/db/DatabaseConnection.java`:

```java
private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/inventory_management";
private static final String DATABASE_USER = "your_username";
private static final String DATABASE_PASSWORD = "your_password";
```

### Step 3: Build and Run

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn javafx:run
```

## Project Structure

```
basic-inventory-management-system/
├── src/main/
│   ├── java/com/example/dummy_inventory/
│   │   ├── controller/          # JavaFX Controllers
│   │   │   ├── LoginController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── ProductsController.java
│   │   │   ├── SalesController.java
│   │   │   ├── CategoriesController.java
│   │   │   ├── SuppliersController.java
│   │   │   ├── UserManagementController.java
│   │   │   └── ReportsController.java
│   │   ├── model/               # Data Models
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── Supplier.java
│   │   │   └── Sale.java
│   │   ├── dao/                 # Data Access Objects
│   │   │   ├── UserDAO.java
│   │   │   ├── ProductDAO.java
│   │   │   ├── CategoryDAO.java
│   │   │   ├── SupplierDAO.java
│   │   │   ├── SaleDAO.java
│   │   │   └── ReportsDAO.java
│   │   ├── db/                  # Database Connection
│   │   │   └── DatabaseConnection.java
│   │   ├── util/                # Utility Classes
│   │   │   ├── CSVExporter.java
│   │   │   └── CSVImporter.java
│   │   └── HelloApplication.java
│   └── resources/
│       └── fxml/                # FXML View Files
│           ├── LoginView.fxml
│           ├── DashboardView.fxml
│           ├── ProductsView.fxml
│           ├── SalesView.fxml
│           ├── CategoriesView.fxml
│           └── SuppliersView.fxml
├── database_schema.sql          # Complete DB Schema
├── pom.xml                      # Maven Configuration
└── README.md                    # This file
```

## User Roles & Permissions

### ADMIN
- Full system access
- User management (create, edit, delete users)
- All inventory operations
- All reports and analytics
- System settings

### MANAGER
- Inventory management
- Sales processing
- Reports and analytics
- Cannot manage users

### USER
- View products and categories
- Process sales
- Basic operations only

## Key Features Breakdown

### Password Security
- Passwords hashed using BCrypt (salt + hash)
- No plain text password storage
- Secure password verification

### Transaction Management
- Sales use database transactions
- Stock automatically updated on sale
- Stock restored on sale deletion
- Rollback on errors

### Search Functionality
- Products search by name
- Categories search by name
- Suppliers search by name/contact
- Users search by username/full name

### Validation
- Input validation on all forms
- Email format validation
- Stock quantity validation
- Price validation
- Required field checks

### Error Handling
- Try-catch blocks throughout
- User-friendly error messages
- Console logging for debugging
- SQL exception handling

## Dependencies

```xml
<!-- Core Dependencies -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.6</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<!-- CSV Export/Import -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>

<!-- Excel Support -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## Common Operations

### Creating a New User (Admin Only)
1. Navigate to User Management
2. Enter username, password, full name, email
3. Select role (ADMIN/MANAGER/USER)
4. Click "Create User"

### Recording a Sale
1. Navigate to Sales
2. Select product from dropdown
3. Enter quantity
4. System validates stock availability
5. Click "Record Sale"
6. Stock automatically updated

### Generating Reports
1. Navigate to Reports
2. Select report type
3. Set date range if needed
4. Click "Generate Report"
5. Export to CSV if needed

### Exporting Data
1. Navigate to desired module
2. Click "Export" button
3. Choose file location
4. Data exported to CSV format

## Troubleshooting

### Database Connection Failed
- Verify MySQL is running
- Check database credentials
- Ensure `inventory_management` database exists
- Check port 3306 is not blocked

### Login Not Working
- Verify user exists in database
- Password is `admin123` for default users
- Check user `is_active` status
- Review console for error messages

### Application Won't Start
- Check Java version (must be 9+)
- Run `mvn clean compile`
- Verify all dependencies downloaded
- Check console for exceptions

## Future Enhancements

Potential features to add:
- Barcode scanning
- Product images
- Invoice generation
- Email notifications
- Dark mode theme
- Multi-location support
- Dashboard charts/graphs
- Batch operations
- Advanced filters
- Receipt printing

## Technologies Used

- **Language**: Java 9+
- **UI Framework**: JavaFX 17
- **Database**: MySQL 8
- **Build Tool**: Maven
- **Security**: BCrypt
- **Data Export**: OpenCSV, Apache POI

## Author & License

Developed as a comprehensive inventory management solution for small to medium businesses.

---

## Quick Start Guide

1. **Install MySQL** and create database using `database_schema.sql`
2. **Configure** database credentials in `DatabaseConnection.java`
3. **Build** project: `mvn clean compile`
4. **Run** application: `mvn javafx:run`
5. **Login** with: username `admin`, password `admin123`
6. **Explore** the dashboard and various modules

For detailed documentation, see the inline comments in each source file.

## Support

For issues or questions:
1. Check console logs for error details
2. Verify database connection
3. Ensure all dependencies are installed
4. Review this README for common solutions
