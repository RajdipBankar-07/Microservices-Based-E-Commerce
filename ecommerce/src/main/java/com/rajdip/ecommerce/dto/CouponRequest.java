package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request body for creating / updating a coupon (ADMIN only).
 */
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Pattern(regexp = "^[A-Z0-9_-]{3,50}$",
             message = "Code must be 3–50 uppercase letters, digits, underscores or hyphens")
    private String code;

    private String description;

    @NotBlank(message = "Discount type is required (PERCENTAGE or FIXED)")
    @Pattern(regexp = "^(PERCENTAGE|FIXED)$", message = "discountType must be PERCENTAGE or FIXED")
    private String discountType;

    @Positive(message = "Discount value must be greater than 0")
    private double discountValue;

    /** Max discount cap for PERCENTAGE type (0 = no cap) */
    @PositiveOrZero
    private double maxDiscountAmount = 0;

    /** Minimum order total required (0 = no minimum) */
    @PositiveOrZero
    private double minOrderAmount = 0;

    /** Max total uses (0 = unlimited) */
    @PositiveOrZero
    private int maxUses = 0;

    @NotNull(message = "Expiry date is required")
    @FutureOrPresent(message = "Expiry date must be today or future")
    private LocalDate expiryDate;

    private boolean isActive = true;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String    getCode()               { return code; }
    public void      setCode(String c)       { this.code = c; }
    public String    getDescription()        { return description; }
    public void      setDescription(String d){ this.description = d; }
    public String    getDiscountType()       { return discountType; }
    public void      setDiscountType(String t){ this.discountType = t; }
    public double    getDiscountValue()      { return discountValue; }
    public void      setDiscountValue(double v){ this.discountValue = v; }
    public double    getMaxDiscountAmount()  { return maxDiscountAmount; }
    public void      setMaxDiscountAmount(double m){ this.maxDiscountAmount = m; }
    public double    getMinOrderAmount()     { return minOrderAmount; }
    public void      setMinOrderAmount(double m){ this.minOrderAmount = m; }
    public int       getMaxUses()            { return maxUses; }
    public void      setMaxUses(int m)       { this.maxUses = m; }
    public LocalDate getExpiryDate()         { return expiryDate; }
    public void      setExpiryDate(LocalDate d){ this.expiryDate = d; }
    public boolean   isActive()              { return isActive; }
    public void      setActive(boolean a)    { this.isActive = a; }
}
