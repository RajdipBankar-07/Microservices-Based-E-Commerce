package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "coupon_usages")
@CompoundIndex(name = "uq_coupon_usage_user_coupon",
               def = "{'user.$id': 1, 'coupon.$id': 1}",
               unique = true)
public class CouponUsage {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"currentUses", "maxUses"})
    private Coupon coupon;

    @DBRef
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    @DBRef
    @JsonIgnoreProperties({"user", "product"})
    private Order order;

    private double originalAmount;
    private double discountAmount;
    private double finalAmount;
    private LocalDateTime usedAt = LocalDateTime.now();

    public Long          getId()             { return id; }
    public Coupon        getCoupon()         { return coupon; }
    public User          getUser()           { return user; }
    public Order         getOrder()          { return order; }
    public double        getOriginalAmount() { return originalAmount; }
    public double        getDiscountAmount() { return discountAmount; }
    public double        getFinalAmount()    { return finalAmount; }
    public LocalDateTime getUsedAt()         { return usedAt; }

    public void setId(Long id)                   { this.id = id; }
    public void setCoupon(Coupon c)              { this.coupon = c; }
    public void setUser(User u)                  { this.user = u; }
    public void setOrder(Order o)                { this.order = o; }
    public void setOriginalAmount(double a)      { this.originalAmount = a; }
    public void setDiscountAmount(double d)      { this.discountAmount = d; }
    public void setFinalAmount(double f)         { this.finalAmount = f; }
    public void setUsedAt(LocalDateTime t)       { this.usedAt = t; }
}
