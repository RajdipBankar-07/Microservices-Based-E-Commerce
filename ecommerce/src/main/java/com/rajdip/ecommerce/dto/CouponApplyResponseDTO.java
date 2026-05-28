package com.rajdip.ecommerce.dto;

/**
 * Response after successfully applying a coupon.
 * Shows the user exactly how much they saved.
 */
public class CouponApplyResponseDTO {

    private String couponCode;
    private String discountType;
    private double discountValue;
    private double originalAmount;
    private double discountAmount;    // ₹ saved
    private double finalAmount;       // amount to pay
    private String message;

    public CouponApplyResponseDTO(String couponCode, String discountType, double discountValue,
                                   double originalAmount, double discountAmount, double finalAmount) {
        this.couponCode      = couponCode;
        this.discountType    = discountType;
        this.discountValue   = discountValue;
        this.originalAmount  = originalAmount;
        this.discountAmount  = discountAmount;
        this.finalAmount     = finalAmount;
        this.message         = String.format(
                "🎉 Coupon applied! You saved ₹%.2f — pay ₹%.2f",
                discountAmount, finalAmount);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getCouponCode()       { return couponCode; }
    public String getDiscountType()     { return discountType; }
    public double getDiscountValue()    { return discountValue; }
    public double getOriginalAmount()   { return originalAmount; }
    public double getDiscountAmount()   { return discountAmount; }
    public double getFinalAmount()      { return finalAmount; }
    public String getMessage()          { return message; }
}
