package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private int quantity;

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }

    public Product getProduct() { return product; }

    public int getQuantity() { return quantity; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setUser(User user) { this.user = user; }

    public void setProduct(Product product) { this.product = product; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
