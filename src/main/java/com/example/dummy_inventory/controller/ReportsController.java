package com.example.dummy_inventory.controller;

import com.example.dummy_inventory.dao.*;
import com.example.dummy_inventory.model.*;
import com.example.dummy_inventory.util.CSVExporter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportsController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalCategoriesLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label inventoryValueLabel;
    @FXML private Label totalProfitLabel;

    @FXML private TextArea reportTextArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private Label statusLabel;

    private ReportsDAO reportsDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private SaleDAO saleDAO;

    @FXML
    public void initialize() {
        reportsDAO = new ReportsDAO();
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        saleDAO = new SaleDAO();

        // Check permissions
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.canViewReports()) {
            showError("Access Denied: Manager/Admin privileges required");

            // Disable all controls to prevent unauthorized access
            disableAllControls();

            // Clear all data displays
            clearAllData();
            return;
        }

        // Setup report types
        reportTypeComboBox.getItems().addAll(
            "Top Selling Products",
            "Sales by Category",
            "Low Stock Report",
            "Inventory Valuation",
            "Monthly Sales Summary"
        );

        // Load dashboard stats
        loadDashboardStats();
    }

    /**
     * Disable all controls when user doesn't have permission
     */
    private void disableAllControls() {
        if (reportTypeComboBox != null) reportTypeComboBox.setDisable(true);
        if (startDatePicker != null) startDatePicker.setDisable(true);
        if (endDatePicker != null) endDatePicker.setDisable(true);
        if (reportTextArea != null) reportTextArea.setDisable(true);
    }

    /**
     * Clear all data displays when user doesn't have permission
     */
    private void clearAllData() {
        if (totalRevenueLabel != null) totalRevenueLabel.setText("N/A");
        if (totalProductsLabel != null) totalProductsLabel.setText("N/A");
        if (totalCategoriesLabel != null) totalCategoriesLabel.setText("N/A");
        if (lowStockCountLabel != null) lowStockCountLabel.setText("N/A");
        if (inventoryValueLabel != null) inventoryValueLabel.setText("N/A");
        if (totalProfitLabel != null) totalProfitLabel.setText("N/A");
        if (reportTextArea != null) reportTextArea.setText("Access denied. This feature requires Manager or Admin privileges.");
    }

    private void loadDashboardStats() {
        totalRevenueLabel.setText("$" + String.format("%.2f", reportsDAO.getTotalRevenue()));
        totalProductsLabel.setText(String.valueOf(reportsDAO.getTotalProductCount()));
        totalCategoriesLabel.setText(String.valueOf(reportsDAO.getTotalCategoryCount()));
        lowStockCountLabel.setText(String.valueOf(reportsDAO.getLowStockProductCount(20)));
        inventoryValueLabel.setText("$" + String.format("%.2f", reportsDAO.getTotalInventoryValue()));
        totalProfitLabel.setText("$" + String.format("%.2f", reportsDAO.getTotalProfit()));
    }

    @FXML
    private void handleGenerateReport() {
        // Verify permissions before generating report
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.canViewReports()) {
            showError("Access Denied: Manager/Admin privileges required");
            return;
        }

        String reportType = reportTypeComboBox.getValue();
        if (reportType == null) {
            showError("Please select a report type.");
            return;
        }

        StringBuilder report = new StringBuilder();

        switch (reportType) {
            case "Top Selling Products":
                generateTopSellingReport(report);
                break;
            case "Sales by Category":
                generateSalesByCategoryReport(report);
                break;
            case "Low Stock Report":
                generateLowStockReport(report);
                break;
            case "Inventory Valuation":
                generateInventoryValuationReport(report);
                break;
            case "Monthly Sales Summary":
                generateMonthlySalesReport(report);
                break;
        }

        reportTextArea.setText(report.toString());
        showSuccess("Report generated successfully!");
    }

    private void generateTopSellingReport(StringBuilder report) {
        report.append("=== TOP 10 SELLING PRODUCTS ===\n\n");
        Map<String, Object> topProducts = reportsDAO.getTopSellingProducts(10);
        int rank = 1;
        for (Map.Entry<String, Object> entry : topProducts.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) entry.getValue();
            report.append(String.format("%d. %s\n", rank++, entry.getKey()));
            report.append(String.format("   Quantity Sold: %d\n", data.get("total_sold")));
            report.append(String.format("   Revenue: $%.2f\n\n", data.get("revenue")));
        }
    }

    private void generateSalesByCategoryReport(StringBuilder report) {
        report.append("=== SALES BY CATEGORY ===\n\n");
        Map<String, Double> salesByCategory = reportsDAO.getSalesByCategory();
        for (Map.Entry<String, Double> entry : salesByCategory.entrySet()) {
            report.append(String.format("%s: $%.2f\n", entry.getKey(), entry.getValue()));
        }
    }

    private void generateLowStockReport(StringBuilder report) {
        report.append("=== LOW STOCK ALERT ===\n\n");
        List<Map<String, Object>> lowStockItems = reportsDAO.getLowStockReport(20);
        for (Map<String, Object> item : lowStockItems) {
            report.append(String.format("Product: %s\n", item.get("product_name")));
            report.append(String.format("Current Stock: %d\n", item.get("stock")));
            report.append(String.format("Reorder Level: %d\n", item.get("reorder_level")));
            report.append(String.format("Category: %s\n", item.get("category")));
            report.append(String.format("Supplier: %s\n\n", item.get("supplier")));
        }
    }

    private void generateInventoryValuationReport(StringBuilder report) {
        report.append("=== INVENTORY VALUATION REPORT ===\n\n");
        report.append(String.format("Total Inventory Value: $%.2f\n", reportsDAO.getTotalInventoryValue()));
        report.append(String.format("Total Products: %d\n", reportsDAO.getTotalProductCount()));
        report.append(String.format("Total Categories: %d\n\n", reportsDAO.getTotalCategoryCount()));

        Map<String, Integer> productsByCategory = reportsDAO.getProductCountByCategory();
        report.append("Products by Category:\n");
        for (Map.Entry<String, Integer> entry : productsByCategory.entrySet()) {
            report.append(String.format("  %s: %d products\n", entry.getKey(), entry.getValue()));
        }
    }

    private void generateMonthlySalesReport(StringBuilder report) {
        LocalDate now = LocalDate.now();
        report.append(String.format("=== SALES SUMMARY - %s %d ===\n\n",
                                     now.getMonth(), now.getYear()));

        Map<String, Double> monthlySales = reportsDAO.getMonthlySalesReport(
                                                        now.getYear(), now.getMonthValue());
        double total = 0;
        for (Map.Entry<String, Double> entry : monthlySales.entrySet()) {
            report.append(String.format("%s: $%.2f\n", entry.getKey(), entry.getValue()));
            total += entry.getValue();
        }
        report.append(String.format("\nTotal Revenue: $%.2f\n", total));
    }

    @FXML
    private void handleExportProducts() {
        // Verify permissions before exporting
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.canViewReports()) {
            showError("Access Denied: Manager/Admin privileges required");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Products to CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("products_export.csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            List<Product> products = productDAO.getAllProducts();
            if (CSVExporter.exportProducts(products, file.getAbsolutePath())) {
                showSuccess("Products exported successfully!");
            } else {
                showError("Failed to export products.");
            }
        }
    }

    @FXML
    private void handleExportSales() {
        // Verify permissions before exporting
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.canViewReports()) {
            showError("Access Denied: Manager/Admin privileges required");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales to CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("sales_export.csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            List<Sale> sales = saleDAO.getAllSales();
            if (CSVExporter.exportSales(sales, file.getAbsolutePath())) {
                showSuccess("Sales exported successfully!");
            } else {
                showError("Failed to export sales.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardStats();
        showSuccess("Dashboard refreshed!");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
    }
}
