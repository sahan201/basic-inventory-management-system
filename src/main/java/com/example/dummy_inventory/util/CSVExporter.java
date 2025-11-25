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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Product ID", "Name", "Quantity", "Price", "Category ID", "Supplier ID", "Category Name", "Supplier Name"};
            writer.writeNext(header);

            // Write data
            for (Product product : products) {
                String[] data = {
                    String.valueOf(product.getProductId()),
                    product.getName(),
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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Category ID", "Name", "Description"};
            writer.writeNext(header);

            // Write data
            for (Category category : categories) {
                String[] data = {
                    String.valueOf(category.getCategoryId()),
                    category.getName(),
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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Supplier ID", "Name", "Contact Person", "Email"};
            writer.writeNext(header);

            // Write data
            for (Supplier supplier : suppliers) {
                String[] data = {
                    String.valueOf(supplier.getSupplierId()),
                    supplier.getName(),
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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"Sale ID", "Product ID", "Product Name", "Quantity Sold", "Unit Price", "Total Amount", "Sale Date"};
            writer.writeNext(header);

            // Write data
            for (Sale sale : sales) {
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
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = {"User ID", "Username", "Role", "Full Name", "Email", "Active", "Created At", "Last Login"};
            writer.writeNext(header);

            // Write data
            for (User user : users) {
                String[] data = {
                    String.valueOf(user.getUserId()),
                    user.getUsername(),
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
