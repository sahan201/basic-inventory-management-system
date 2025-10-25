package com.example.dummy_inventory.model;
import java.time.LocalDateTime;

public class Sale {
    private int saleId;
    private int productId;
    private int quantitySold;
    private LocalDateTime saleDate;

    // Additional fields for display purposes (not in database)
    private String productName;
    private double productPrice;

    // Default constructor
    public Sale() {
    }

    // Constructor with all database fields
    public Sale(int saleId, int productId, int quantitySold, LocalDateTime saleDate) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.saleDate = saleDate;
    }

    // Constructor without saleId (for creating new sales)
    public Sale(int productId, int quantitySold, LocalDateTime saleDate) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.saleDate = saleDate;
    }

    // Constructor for creating sale with current date
    public Sale(int productId, int quantitySold) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.saleDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    // Utility method to calculate total amount
    public double getTotalAmount() {
        return quantitySold * productPrice;
    }

    // toString method
    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", productId=" + productId +
                ", quantitySold=" + quantitySold +
                ", saleDate=" + saleDate +
                '}';
    }
}
