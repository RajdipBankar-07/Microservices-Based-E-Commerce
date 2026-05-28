package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for applying a coupon to an order.
 * Sent by the user before payment.
 */
public class CouponApplyRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Coupon code is required")
    private String couponCode;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long   getUserId()              { return userId; }
    public void   setUserId(Long id)       { this.userId = id; }

    public Long   getOrderId()             { return orderId; }
    public void   setOrderId(Long id)      { this.orderId = id; }

    public String getCouponCode()          { return couponCode; }
    public void   setCouponCode(String c)  { this.couponCode = c; }
}
