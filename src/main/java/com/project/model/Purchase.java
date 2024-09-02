package com.project.model;

import java.util.Date;

public class Purchase {
    private Customer customer;
    private Product product;
    private Date purchaseDate;


    public Purchase() {
    }

    public Purchase(Customer customer, Product product, Date purchaseDate) {
        this.customer = customer;
        this.product = product;
        this.purchaseDate = purchaseDate;
    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
