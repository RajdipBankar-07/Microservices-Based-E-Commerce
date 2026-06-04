// src/main/java/com/rajdip/ecommerce/dto/CheckoutResponse.java
package com.rajdip.ecommerce.dto;

import java.util.List;

public class CheckoutResponse {
    private List<Long> orderIds; // IDs of created orders
    private double totalBeforeDiscount;
    private double discountAmount;
    private double finalTotal;
    private String message;

    public CheckoutResponse() {}

    public CheckoutResponse(List<Long> orderIds, double totalBeforeDiscount, double discountAmount, double finalTotal, String message) {
        this.orderIds = orderIds;
        this.totalBeforeDiscount = totalBeforeDiscount;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.message = message;
    }

    public List<Long> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }
    public double getTotalBeforeDiscount() { return totalBeforeDiscount; }
    public void setTotalBeforeDiscount(double totalBeforeDiscount) { this.totalBeforeDiscount = totalBeforeDiscount; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getFinalTotal() { return finalTotal; }
    public void setFinalTotal(double finalTotal) { this.finalTotal = finalTotal; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
