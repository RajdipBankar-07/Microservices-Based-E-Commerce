package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getUserId() { return userId; }

    public Long getProductId() { return productId; }

    public int getQuantity() { return quantity; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setUserId(Long userId) { this.userId = userId; }

    public void setProductId(Long productId) { this.productId = productId; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
