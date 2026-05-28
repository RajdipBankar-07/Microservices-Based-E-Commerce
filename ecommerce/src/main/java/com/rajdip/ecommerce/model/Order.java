package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"password"})
    private User user;

    @DBRef
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String status;

    public Long    getId()       { return id; }
    public User    getUser()     { return user; }
    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }
    public String  getStatus()   { return status; }

    public void setId(Long id)           { this.id = id; }
    public void setUser(User user)       { this.user = user; }
    public void setProduct(Product p)    { this.product = p; }
    public void setQuantity(int q)       { this.quantity = q; }
    public void setStatus(String s)      { this.status = s; }
}