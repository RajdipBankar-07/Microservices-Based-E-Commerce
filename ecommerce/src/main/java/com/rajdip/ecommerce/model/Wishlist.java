package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "wishlists")
public class Wishlist {

    @Id
    private Long id;

    @DBRef
    @Indexed(unique = true)
    @JsonIgnoreProperties({"password"})
    private User user;

    @DBRef
    private Set<Product> products = new HashSet<>();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Set<Product> getProducts() { return products; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setProducts(Set<Product> products) { this.products = products; }
}
