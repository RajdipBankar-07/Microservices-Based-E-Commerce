package com.rajdip.ecommerce.dto;

/**
 * Lightweight DTO returned after a payment status update (simulate gateway callback).
 */
public class PaymentStatusUpdateRequest {

    private String status; // SUCCESS | FAILED

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }
}
