package com.example.dummy_inventory.model;
import java.time.LocalDateTime;

public class Sale {
    // Enum for payment methods
    public enum PaymentMethod {
        CASH, CARD, MOBILE, OTHER
    }

    private int saleId;
    private int productId;
    private int quantitySold;
    private double unitPrice;      // Price at time of sale (from database)
    private double totalAmount;    // Total amount (from database)
    private LocalDateTime saleDate;
    private Integer userId;        // User who made the sale
    private PaymentMethod paymentMethod;
    private String notes;

    // Additional fields for display purposes (not in database)
    private String productName;
    private double productPrice;   // Current product price (for backward compatibility)

    // Default constructor
    public Sale() {
        this.paymentMethod = PaymentMethod.CASH; // Default payment method
    }

    // Constructor with all database fields
    public Sale(int saleId, int productId, int quantitySold, double unitPrice,
                double totalAmount, LocalDateTime saleDate, Integer userId,
                PaymentMethod paymentMethod, String notes) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    // Constructor without saleId (for creating new sales)
    public Sale(int productId, int quantitySold, double unitPrice, LocalDateTime saleDate) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalAmount = quantitySold * unitPrice;
        this.saleDate = saleDate;
        this.paymentMethod = PaymentMethod.CASH;
    }

    // Constructor for creating sale with current date
    public Sale(int productId, int quantitySold, double unitPrice) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalAmount = quantitySold * unitPrice;
        this.saleDate = LocalDateTime.now();
        this.paymentMethod = PaymentMethod.CASH;
    }

    // Backwards compatible constructor (for legacy code)
    public Sale(int saleId, int productId, int quantitySold, LocalDateTime saleDate) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.saleDate = saleDate;
        this.paymentMethod = PaymentMethod.CASH;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility method to calculate total amount (backward compatibility)
    public double calculateTotalAmount() {
        if (totalAmount > 0) {
            return totalAmount;
        }
        return quantitySold * (unitPrice > 0 ? unitPrice : productPrice);
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
