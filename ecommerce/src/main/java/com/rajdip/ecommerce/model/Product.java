package com.rajdip.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {

    @Id
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    private int quantity;

    @DBRef(lazy = true)
    private Category category;

    public Long     getId()          { return id; }
    public String   getName()        { return name; }
    public double   getPrice()       { return price; }
    public int      getQuantity()    { return quantity; }
    public Category getCategory()    { return category; }

    public void setId(Long id)              { this.id = id; }
    public void setName(String name)        { this.name = name; }
    public void setPrice(double price)      { this.price = price; }
    public void setQuantity(int quantity)   { this.quantity = quantity; }
    public void setCategory(Category c)     { this.category = c; }
}