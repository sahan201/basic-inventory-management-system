package com.example.dummy_inventory.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrder {
    // Enum for order status
    public enum Status {
        PENDING, RECEIVED, CANCELLED
    }

    private int orderId;
    private int supplierId;
    private int productId;
    private int quantity;
    private double unitCost;
    private double totalCost;
    private LocalDateTime orderDate;
    private LocalDate expectedDelivery;
    private Status status;
    private LocalDateTime receivedDate;
    private Integer userId;
    private String notes;

    // Additional fields for display (not in database)
    private String supplierName;
    private String productName;
    private String userName;

    // Default constructor
    public PurchaseOrder() {
        this.status = Status.PENDING;
    }

    // Constructor with all fields
    public PurchaseOrder(int orderId, int supplierId, int productId, int quantity,
                         double unitCost, double totalCost, LocalDateTime orderDate,
                         LocalDate expectedDelivery, Status status, LocalDateTime receivedDate,
                         Integer userId, String notes) {
        this.orderId = orderId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.orderDate = orderDate;
        this.expectedDelivery = expectedDelivery;
        this.status = status;
        this.receivedDate = receivedDate;
        this.userId = userId;
        this.notes = notes;
    }

    // Constructor for creating new orders
    public PurchaseOrder(int supplierId, int productId, int quantity,
                         double unitCost, LocalDate expectedDelivery, Integer userId, String notes) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = quantity * unitCost;
        this.expectedDelivery = expectedDelivery;
        this.userId = userId;
        this.notes = notes;
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Recalculate total cost when quantity changes
        this.totalCost = this.quantity * this.unitCost;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
        // Recalculate total cost when unit cost changes
        this.totalCost = this.quantity * this.unitCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDelivery() {
        return expectedDelivery;
    }

    public void setExpectedDelivery(LocalDate expectedDelivery) {
        this.expectedDelivery = expectedDelivery;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Display fields
    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "orderId=" + orderId +
                ", supplierId=" + supplierId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", totalCost=" + totalCost +
                ", status=" + status +
                ", orderDate=" + orderDate +
                '}';
    }
}
