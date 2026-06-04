package com.rajdip.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    private String description;
    private String imageUrl;
    private LocalDateTime deactivateAt;

    public Long          getId()           { return id; }
    public String        getName()         { return name; }
    public double        getPrice()        { return price; }
    public int           getQuantity()     { return quantity; }
    public Category      getCategory()     { return category; }
    public String        getDescription()  { return description; }
    public String        getImageUrl()     { return imageUrl; }
    public LocalDateTime getDeactivateAt() { return deactivateAt; }

    public void setId(Long id)                         { this.id = id; }
    public void setName(String name)                   { this.name = name; }
    public void setPrice(double price)                 { this.price = price; }
    public void setQuantity(int quantity)              { this.quantity = quantity; }
    public void setCategory(Category c)                { this.category = c; }
    public void setDescription(String d)               { this.description = d; }
    public void setImageUrl(String url)                { this.imageUrl = url; }
    public void setDeactivateAt(LocalDateTime dateAt) { this.deactivateAt = dateAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}