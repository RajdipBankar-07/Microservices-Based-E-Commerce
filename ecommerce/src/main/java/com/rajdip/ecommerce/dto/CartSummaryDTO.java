package com.rajdip.ecommerce.dto;

import java.util.List;

/**
 * Summary view of a user's cart:
 *  - items  : list of cart items
 *  - totalPrice : sum of (product.price × quantity) for all items
 */
public class CartSummaryDTO {

    private List<?> items;
    private double totalPrice;

    public CartSummaryDTO(List<?> items, double totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public List<?> getItems() { return items; }

    public double getTotalPrice() { return totalPrice; }
}
