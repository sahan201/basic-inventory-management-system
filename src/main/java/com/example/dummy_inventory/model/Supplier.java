package com.example.dummy_inventory.model;

public class Supplier {
    private int supplierId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;

    // Default constructor
    public Supplier() {
    }

    // Constructor with all fields
    public Supplier(int supplierId, String name, String contactPerson, String email, String phone, String address) {
        this.supplierId = supplierId;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Constructor without supplierId (for creating new suppliers)
    public Supplier(String name, String contactPerson, String email, String phone, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Legacy constructor without address (for backward compatibility)
    public Supplier(int supplierId, String name, String contactPerson, String email, String phone) {
        this(supplierId, name, contactPerson, email, phone, null);
    }

    // Legacy constructor without supplierId and address (for backward compatibility)
    public Supplier(String name, String contactPerson, String email, String phone) {
        this(name, contactPerson, email, phone, null);
    }

    // Legacy constructor without phone (for backward compatibility)
    public Supplier(String name, String contactPerson, String email) {
        this(name, contactPerson, email, null, null);
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // toString method
    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", name='" + name + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
