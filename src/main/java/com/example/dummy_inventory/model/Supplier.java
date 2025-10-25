package com.example.dummy_inventory.model;

public class Supplier {
    private int supplierId;
    private String name;
    private String contactPerson;
    private String email;

    // Default constructor
    public Supplier() {
    }

    // Constructor with all fields
    public Supplier(int supplierId, String name, String contactPerson, String email) {
        this.supplierId = supplierId;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
    }

    // Constructor without supplierId (for creating new suppliers)
    public Supplier(String name, String contactPerson, String email) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // toString method
    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", name='" + name + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
