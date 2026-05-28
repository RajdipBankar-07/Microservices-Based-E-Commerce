package com.rajdip.ecommerce.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "coupons")
public class Coupon {

    @Id
    private Long id;

    @Indexed(unique = true)
    @NotBlank(message = "Coupon code is required")
    @Pattern(regexp = "^[A-Z0-9_-]{3,50}$", message = "Code must be 3–50 uppercase letters/digits/underscores/hyphens")
    private String code;

    private String description;

    @NotBlank(message = "Discount type is required (PERCENTAGE or FIXED)")
    private String discountType;

    @Positive(message = "Discount value must be greater than 0")
    private double discountValue;

    @PositiveOrZero
    private double maxDiscountAmount = 0;

    @PositiveOrZero
    private double minOrderAmount = 0;

    @PositiveOrZero
    private int maxUses = 0;

    private int currentUses = 0;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private boolean isActive = true;

    public Long      getId()                { return id; }
    public String    getCode()              { return code; }
    public String    getDescription()       { return description; }
    public String    getDiscountType()      { return discountType; }
    public double    getDiscountValue()     { return discountValue; }
    public double    getMaxDiscountAmount() { return maxDiscountAmount; }
    public double    getMinOrderAmount()    { return minOrderAmount; }
    public int       getMaxUses()           { return maxUses; }
    public int       getCurrentUses()       { return currentUses; }
    public LocalDate getExpiryDate()        { return expiryDate; }
    public boolean   isActive()             { return isActive; }

    public void setId(Long id)                  { this.id = id; }
    public void setCode(String c)               { this.code = c; }
    public void setDescription(String d)        { this.description = d; }
    public void setDiscountType(String t)       { this.discountType = t; }
    public void setDiscountValue(double v)      { this.discountValue = v; }
    public void setMaxDiscountAmount(double m)  { this.maxDiscountAmount = m; }
    public void setMinOrderAmount(double m)     { this.minOrderAmount = m; }
    public void setMaxUses(int m)               { this.maxUses = m; }
    public void setCurrentUses(int c)           { this.currentUses = c; }
    public void setExpiryDate(LocalDate d)      { this.expiryDate = d; }
    public void setActive(boolean a)            { this.isActive = a; }
}
