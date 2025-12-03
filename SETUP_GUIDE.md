# Setup Guide - Inventory Management System

## Quick Fix for Login Issues

### Problem: "Can't log in with correct credentials"

This is almost always caused by a **missing or misconfigured `database.properties` file**.

### Solution (5 minutes):

1. **Create the database.properties file**
   ```bash
   cd src/main/resources/
   cp database.properties.template database.properties
   ```

2. **Edit the file with your MySQL password**
   ```bash
   nano database.properties
   # or
   vim database.properties
   ```

   Update this line:
   ```properties
   db.password=YOUR_MYSQL_PASSWORD
   ```

3. **Ensure MySQL is running and database exists**
   ```bash
   # Start MySQL
   sudo service mysql start
   # or
   sudo systemctl start mysql

   # Import the database schema
   mysql -u root -p < database_schema.sql
   ```

4. **Test the application**
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

5. **Login with default credentials**
   - Username: `admin`
   - Password: `admin123`

---

## Complete Installation Guide

### Prerequisites

Before you begin, ensure you have:

- ✅ Java Development Kit (JDK) 9 or higher
- ✅ MySQL Server 5.7+ or 8.0+
- ✅ Maven 3.6+
- ✅ Git (for version control)

#### Check Prerequisites

```bash
# Check Java version
java -version  # Should show 9+

# Check Maven version
mvn -version

# Check MySQL
mysql --version
```

### Step-by-Step Installation

#### 1. Clone or Download Repository

```bash
git clone <repository-url>
cd basic-inventory-management-system
```

#### 2. Install MySQL (if not installed)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

**macOS:**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
- Download from [MySQL Downloads](https://dev.mysql.com/downloads/)
- Run installer and follow wizard
- Remember the root password you set!

#### 3. Create Database

**Option A: Using command line**
```bash
mysql -u root -p < database_schema.sql
```

**Option B: Manual steps**
```bash
# Login to MySQL
mysql -u root -p

# Run these commands in MySQL prompt
CREATE DATABASE inventory_management;
USE inventory_management;
SOURCE database_schema.sql;

# Verify tables were created
SHOW TABLES;

# Should show:
# +----------------------------------+
# | Tables_in_inventory_management   |
# +----------------------------------+
# | AuditLog                         |
# | Category                         |
# | Product                          |
# | PurchaseOrder                    |
# | Sale                             |
# | Supplier                         |
# | SystemSettings                   |
# | User                             |
# +----------------------------------+

# Verify default users exist
SELECT username, role FROM User;

# Should show:
# +----------+---------+
# | username | role    |
# +----------+---------+
# | admin    | ADMIN   |
# | manager  | MANAGER |
# | user     | USER    |
# +----------+---------+

exit;
```

#### 4. Configure Database Connection

```bash
cd src/main/resources/

# Copy the template
cp database.properties.template database.properties

# Edit with your favorite editor
nano database.properties
```

**Update these lines:**
```properties
db.url=jdbc:mysql://localhost:3306/inventory_management
db.user=root
db.password=YOUR_ACTUAL_MYSQL_PASSWORD
db.connection.params=?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

#### 5. Build and Run

```bash
# Go back to project root
cd ../../..

# Clean and compile
mvn clean compile

# Run the application
mvn javafx:run
```

#### 6. Login

Use any of these default accounts:

| Username | Password   | Role    | Access Level                          |
|----------|-----------|---------|---------------------------------------|
| admin    | admin123  | ADMIN   | Full access including user management |
| manager  | admin123  | MANAGER | Inventory, sales, and reports         |
| user     | admin123  | USER    | Basic operations only                 |

---

## Common Issues and Solutions

### Issue 1: "RuntimeException: database.properties file not found"

**Cause**: Missing database.properties file

**Solution**:
```bash
cd src/main/resources/
cp database.properties.template database.properties
# Edit database.properties with your MySQL credentials
```

### Issue 2: "Communications link failure" or "Connection refused"

**Cause**: MySQL server is not running

**Solution**:
```bash
# Ubuntu/Debian
sudo systemctl start mysql
sudo systemctl status mysql

# macOS
brew services start mysql

# Windows
# Start MySQL from Services panel or MySQL Workbench
```

### Issue 3: "Access denied for user 'root'@'localhost'"

**Cause**: Incorrect password in database.properties

**Solution**:
1. Find your actual MySQL root password
2. Update `db.password` in `database.properties`

**Reset MySQL root password if forgotten:**
```bash
# Ubuntu/Debian
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'newpassword';
FLUSH PRIVILEGES;
exit;
```

### Issue 4: "Unknown database 'inventory_management'"

**Cause**: Database was not created

**Solution**:
```bash
mysql -u root -p < database_schema.sql
```

### Issue 5: Login shows "Invalid username or password"

**Possible causes and solutions**:

1. **Database not populated**
   ```bash
   mysql -u root -p inventory_management
   SELECT * FROM User;
   # If empty, run: source database_schema.sql;
   ```

2. **Wrong credentials**
   - Default password is `admin123` (not `admin`)
   - Username is case-sensitive

3. **User is inactive**
   ```bash
   mysql -u root -p inventory_management
   UPDATE User SET is_active = TRUE WHERE username = 'admin';
   ```

### Issue 6: "No suitable driver found for jdbc:mysql"

**Cause**: MySQL connector dependency not downloaded

**Solution**:
```bash
mvn clean install -U
```

### Issue 7: Application window doesn't open

**Cause**: JavaFX not properly configured or wrong Java version

**Solution**:
```bash
# Check Java version
java -version  # Must be 9+

# Reinstall dependencies
mvn clean install

# Try running with verbose output
mvn javafx:run -X
```

---

## Testing Database Connection

You can test your database connection before running the full application:

```bash
# Compile the project
mvn compile

# Run the DatabaseConnection test
mvn exec:java -Dexec.mainClass="com.example.dummy_inventory.db.DatabaseConnection"
```

**Expected output:**
```
Testing database connection...
================================
Database configuration loaded successfully from database.properties
✓ Database connection successful!
Database: inventory_management
User: root@localhost
Driver: MySQL Connector/J
================================
```

---

## Verifying Installation

### 1. Check Database Tables

```bash
mysql -u root -p inventory_management

SHOW TABLES;
SELECT COUNT(*) FROM User;      # Should show 3
SELECT COUNT(*) FROM Category;  # Should show 8
SELECT COUNT(*) FROM Product;   # Should show 15
```

### 2. Test Login

1. Run application: `mvn javafx:run`
2. Login with: `admin` / `admin123`
3. You should see the dashboard with statistics

### 3. Test Basic Operations

- ✅ View products list
- ✅ Add a new category
- ✅ Add a new product
- ✅ Record a sale
- ✅ Generate a report

---

## Security Notes

### Important Security Considerations

1. **Change default passwords immediately** in production
   ```sql
   UPDATE User SET password = '$2a$10$NEW_BCRYPT_HASH' WHERE username = 'admin';
   ```

2. **database.properties is in .gitignore**
   - Never commit this file to version control
   - Contains sensitive credentials

3. **Create production admin account**
   ```sql
   -- After logging in, use User Management to create new admin
   -- Then delete or disable the default 'admin' account
   UPDATE User SET is_active = FALSE WHERE username = 'admin';
   ```

---

## Next Steps After Installation

1. **Change Default Passwords**
   - Login as admin
   - Go to User Management
   - Update passwords for all default accounts

2. **Configure System Settings**
   - Set your company name
   - Configure currency
   - Set tax rates

3. **Import Your Data**
   - Use CSV import features
   - Or manually add categories and suppliers

4. **Create User Accounts**
   - Create accounts for your team members
   - Assign appropriate roles

5. **Explore Features**
   - Try recording sample sales
   - Generate reports
   - Test product management

---

## Getting Help

If you're still experiencing issues:

1. **Check console output** for detailed error messages
2. **Review application logs** if configured
3. **Verify all steps** in this guide were completed
4. **Check MySQL logs**: `/var/log/mysql/error.log` (Linux)

---

## Development Setup

If you want to modify the code:

```bash
# Import into IDE (IntelliJ IDEA recommended)
1. File → Open → Select pom.xml
2. Load as Maven project
3. Wait for dependencies to download
4. Mark src/main/java as Sources Root
5. Mark src/main/resources as Resources Root

# Or use Eclipse
mvn eclipse:eclipse
```

---

## Production Deployment

For production use:

1. Change all default passwords
2. Use strong passwords (12+ characters)
3. Configure SSL for MySQL connection
4. Set up regular database backups
5. Enable audit logging
6. Review and restrict user permissions
7. Consider using a connection pool (HikariCP)

---

**Last Updated**: December 2024

For additional help, see README.md or review inline code documentation.
