package com.example.dummy_inventory.model;

public class Product {
    private int productId;
    private String name;
    private String description;
    private int quantityInStock;
    private double price;
    private double costPrice;
    private int categoryId;
    private int supplierId;
    private int reorderLevel;
    private String barcode;
    private String sku;

    // Additional fields for display purposes (not in database)
    private String categoryName;
    private String supplierName;

    // Default constructor
    public Product() {
    }

    // Constructor with all database fields
    public Product(int productId, String name, int quantityInStock, double price,
                   int categoryId, int supplierId) {
        this.productId = productId;
        this.name = name;
        this.quantityInStock = quantityInStock;
        this.price = price;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
    }

    // Constructor without productId (for creating new products)
    public Product(String name, int quantityInStock, double price,
                   int categoryId, int supplierId) {
        this.name = name;
        this.quantityInStock = quantityInStock;
        this.price = price;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    // Utility method to calculate total value
    public double getTotalValue() {
        return quantityInStock * price;
    }

    // Utility method to calculate profit margin
    public double getProfitMargin() {
        if (costPrice > 0) {
            return ((price - costPrice) / costPrice) * 100;
        }
        return 0.0;
    }

    // Utility method to check if reorder is needed
    public boolean needsReorder() {
        return quantityInStock <= reorderLevel;
    }

    // toString method
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", quantityInStock=" + quantityInStock +
                ", price=" + price +
                ", categoryId=" + categoryId +
                ", supplierId=" + supplierId +
                '}';
    }
}
