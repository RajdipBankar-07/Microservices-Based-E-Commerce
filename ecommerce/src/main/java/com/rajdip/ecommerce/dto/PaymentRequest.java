package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for initiating a payment for an order.
 */
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    /**
     * Allowed values: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH_ON_DELIVERY
     */
    @NotBlank(message = "Payment method is required")
    @Pattern(
        regexp = "CREDIT_CARD|DEBIT_CARD|UPI|NET_BANKING|CASH_ON_DELIVERY",
        message = "Invalid payment method. Use: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH_ON_DELIVERY"
    )
    private String paymentMethod;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getOrderId()              { return orderId; }
    public void setOrderId(Long orderId)  { this.orderId = orderId; }

    public String getPaymentMethod()               { return paymentMethod; }
    public void setPaymentMethod(String method)    { this.paymentMethod = method; }
}
