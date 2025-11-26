package com.example.dummy_inventory.util;

import com.example.dummy_inventory.model.*;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean exportProducts(List<Product> products, String filePath) {
        // Null check for products list
        if (products == null) {
            System.err.println("Error: Cannot export null products list");
            return false;
        }

        // Null check for file path
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Invalid file path");
            return false;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Product ID", "Name", "Quantity", "Price", "Category ID", "Supplier ID", "Category Name", "Supplier Name"};
            writer.writeNext(header);

            // Write data
            for (Product product : products) {
                if (product == null) continue; // Skip null entries

                String[] data = {
                    String.valueOf(product.getProductId()),
                    product.getName() != null ? product.getName() : "",
                    String.valueOf(product.getQuantityInStock()),
                    String.valueOf(product.getPrice()),
                    String.valueOf(product.getCategoryId()),
                    String.valueOf(product.getSupplierId()),
                    product.getCategoryName() != null ? product.getCategoryName() : "",
                    product.getSupplierName() != null ? product.getSupplierName() : ""
                };
                writer.writeNext(data);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error exporting products to CSV:");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportCategories(List<Category> categories, String filePath) {
        // Null check for categories list
        if (categories == null) {
            System.err.println("Error: Cannot export null categories list");
            return false;
        }

        // Null check for file path
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Invalid file path");
            return false;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Category ID", "Name", "Description"};
            writer.writeNext(header);

            // Write data
            for (Category category : categories) {
                if (category == null) continue; // Skip null entries

                String[] data = {
                    String.valueOf(category.getCategoryId()),
                    category.getName() != null ? category.getName() : "",
                    category.getDescription() != null ? category.getDescription() : ""
                };
                writer.writeNext(data);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error exporting categories to CSV:");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportSuppliers(List<Supplier> suppliers, String filePath) {
        // Null check for suppliers list
        if (suppliers == null) {
            System.err.println("Error: Cannot export null suppliers list");
            return false;
        }

        // Null check for file path
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Invalid file path");
            return false;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Supplier ID", "Name", "Contact Person", "Email"};
            writer.writeNext(header);

            // Write data
            for (Supplier supplier : suppliers) {
                if (supplier == null) continue; // Skip null entries

                String[] data = {
                    String.valueOf(supplier.getSupplierId()),
                    supplier.getName() != null ? supplier.getName() : "",
                    supplier.getContactPerson() != null ? supplier.getContactPerson() : "",
                    supplier.getEmail() != null ? supplier.getEmail() : ""
                };
                writer.writeNext(data);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error exporting suppliers to CSV:");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportSales(List<Sale> sales, String filePath) {
        // Null check for sales list
        if (sales == null) {
            System.err.println("Error: Cannot export null sales list");
            return false;
        }

        // Null check for file path
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Invalid file path");
            return false;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Sale ID", "Product ID", "Product Name", "Quantity Sold", "Unit Price", "Total Amount", "Sale Date"};
            writer.writeNext(header);

            // Write data
            for (Sale sale : sales) {
                if (sale == null) continue; // Skip null entries

                String[] data = {
                    String.valueOf(sale.getSaleId()),
                    String.valueOf(sale.getProductId()),
                    sale.getProductName() != null ? sale.getProductName() : "",
                    String.valueOf(sale.getQuantitySold()),
                    String.valueOf(sale.getProductPrice()),
                    String.valueOf(sale.getTotalAmount()),
                    sale.getSaleDate() != null ? sale.getSaleDate().format(DATE_FORMAT) : ""
                };
                writer.writeNext(data);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error exporting sales to CSV:");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportUsers(List<User> users, String filePath) {
        // Null check for users list
        if (users == null) {
            System.err.println("Error: Cannot export null users list");
            return false;
        }

        // Null check for file path
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Invalid file path");
            return false;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"User ID", "Username", "Role", "Full Name", "Email", "Active", "Created At", "Last Login"};
            writer.writeNext(header);

            // Write data
            for (User user : users) {
                if (user == null) continue; // Skip null entries

                String[] data = {
                    String.valueOf(user.getUserId()),
                    user.getUsername() != null ? user.getUsername() : "",
                    user.getRole() != null ? user.getRole().name() : "USER",
                    user.getFullName() != null ? user.getFullName() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    String.valueOf(user.isActive()),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMAT) : "",
                    user.getLastLogin() != null ? user.getLastLogin().format(DATE_FORMAT) : ""
                };
                writer.writeNext(data);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error exporting users to CSV:");
            e.printStackTrace();
            return false;
        }
    }
}
