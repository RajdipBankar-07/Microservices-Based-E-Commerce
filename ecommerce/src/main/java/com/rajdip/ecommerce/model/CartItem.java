package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_items")
@CompoundIndex(name = "uq_cart_user_product", def = "{'user.$id': 1, 'product.$id': 1}", unique = true)
public class CartItem {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"password"})
    private User user;

    @DBRef
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public Long    getId()       { return id; }
    public User    getUser()     { return user; }
    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }

    public void setId(Long id)            { this.id = id; }
    public void setUser(User user)        { this.user = user; }
    public void setProduct(Product p)     { this.product = p; }
    public void setQuantity(int qty)      { this.quantity = qty; }
}
