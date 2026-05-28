package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payments")
public class Payment {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"user", "product"})
    private Order order;

    @DBRef
    @JsonIgnoreProperties({"password"})
    private User user;

    private double amount;
    private String paymentMethod;
    private String status = "PENDING";

    @Indexed(unique = true)
    private String transactionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long          getId()            { return id; }
    public Order         getOrder()         { return order; }
    public User          getUser()          { return user; }
    public double        getAmount()        { return amount; }
    public String        getPaymentMethod() { return paymentMethod; }
    public String        getStatus()        { return status; }
    public String        getTransactionId() { return transactionId; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    public void setId(Long id)                    { this.id = id; }
    public void setOrder(Order o)                 { this.order = o; }
    public void setUser(User u)                   { this.user = u; }
    public void setAmount(double a)               { this.amount = a; }
    public void setPaymentMethod(String m)        { this.paymentMethod = m; }
    public void setStatus(String s)               { this.status = s; }
    public void setTransactionId(String t)        { this.transactionId = t; }
    public void setCreatedAt(LocalDateTime dt)    { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt)    { this.updatedAt = dt; }
}
