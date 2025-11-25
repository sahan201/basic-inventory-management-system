package com.example.dummy_inventory.util;

import com.example.dummy_inventory.model.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<Product> importProducts(String filePath) {
        List<Product> products = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 6) {
                    Product product = new Product();
                    product.setName(row[1]);
                    product.setQuantityInStock(Integer.parseInt(row[2]));
                    product.setPrice(Double.parseDouble(row[3]));
                    product.setCategoryId(Integer.parseInt(row[4]));
                    product.setSupplierId(Integer.parseInt(row[5]));
                    products.add(product);
                }
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error importing products from CSV:");
            e.printStackTrace();
        }

        return products;
    }

    public static List<Category> importCategories(String filePath) {
        List<Category> categories = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 2) {
                    Category category = new Category();
                    category.setName(row[1]);
                    if (row.length > 2) {
                        category.setDescription(row[2]);
                    }
                    categories.add(category);
                }
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error importing categories from CSV:");
            e.printStackTrace();
        }

        return categories;
    }

    public static List<Supplier> importSuppliers(String filePath) {
        List<Supplier> suppliers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 2) {
                    Supplier supplier = new Supplier();
                    supplier.setName(row[1]);
                    if (row.length > 2) supplier.setContactPerson(row[2]);
                    if (row.length > 3) supplier.setEmail(row[3]);
                    suppliers.add(supplier);
                }
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error importing suppliers from CSV:");
            e.printStackTrace();
        }

        return suppliers;
    }
}
