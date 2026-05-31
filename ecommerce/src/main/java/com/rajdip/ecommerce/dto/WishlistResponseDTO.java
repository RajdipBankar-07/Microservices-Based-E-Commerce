package com.rajdip.ecommerce.dto;

import com.rajdip.ecommerce.model.Product;
import java.util.Set;

public class WishlistResponseDTO {
    private Long id;
    private String userEmail;
    private Set<Product> products;

    public WishlistResponseDTO(Long id, String userEmail, Set<Product> products) {
        this.id = id;
        this.userEmail = userEmail;
        this.products = products;
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public Set<Product> getProducts() { return products; }

    public void setId(Long id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setProducts(Set<Product> products) { this.products = products; }
}
