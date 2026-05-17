package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Payment entity — one Payment per Order.
 *
 * Payment Status flow:
 *   PENDING  →  SUCCESS  (payment confirmed)
 *   PENDING  →  FAILED   (payment declined)
 *   SUCCESS  →  REFUNDED (user refund)
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each order can have exactly one payment
    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"user", "product"})
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User user;

    /** Amount paid = product.price × order.quantity at time of payment */
    @Column(nullable = false)
    private double amount;

    /**
     * Payment method chosen by the user.
     * Allowed: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH_ON_DELIVERY
     */
    @Column(nullable = false)
    private String paymentMethod;

    /**
     * PENDING | SUCCESS | FAILED | REFUNDED
     */
    @Column(nullable = false)
    private String status = "PENDING";

    /**
     * Simulated gateway transaction ID — generated on initiation.
     * Format: TXN-<orderId>-<epochMillis>
     */
    @Column(unique = true)
    private String transactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()               { return id; }
    public Order getOrder()           { return order; }
    public User getUser()             { return user; }
    public double getAmount()         { return amount; }
    public String getPaymentMethod()  { return paymentMethod; }
    public String getStatus()         { return status; }
    public String getTransactionId()  { return transactionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setId(Long id)                       { this.id = id; }
    public void setOrder(Order order)                { this.order = order; }
    public void setUser(User user)                   { this.user = user; }
    public void setAmount(double amount)             { this.amount = amount; }
    public void setPaymentMethod(String m)           { this.paymentMethod = m; }
    public void setStatus(String status)             { this.status = status; }
    public void setTransactionId(String txn)         { this.transactionId = txn; }
    public void setCreatedAt(LocalDateTime dt)       { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt)       { this.updatedAt = dt; }
}
