package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Review entity — one review per user per product.
 *
 * Rating scale : 1 (worst) → 5 (best)
 * Comment      : optional free-text feedback
 * Verified     : true only when the reviewer has actually ordered this product
 */
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "product_id"},
        name = "uq_user_product_review"
    )
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"category"})
    private Product product;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private int rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    @Column(length = 1000)
    private String comment;

    /** true = reviewer has purchased this product (verified buyer badge) */
    @Column(nullable = false)
    private boolean verifiedPurchase = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public User getUser()                { return user; }
    public Product getProduct()          { return product; }
    public int getRating()               { return rating; }
    public String getComment()           { return comment; }
    public boolean isVerifiedPurchase()  { return verifiedPurchase; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setId(Long id)                       { this.id = id; }
    public void setUser(User user)                   { this.user = user; }
    public void setProduct(Product product)          { this.product = product; }
    public void setRating(int rating)                { this.rating = rating; }
    public void setComment(String comment)           { this.comment = comment; }
    public void setVerifiedPurchase(boolean v)       { this.verifiedPurchase = v; }
    public void setCreatedAt(LocalDateTime dt)       { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt)       { this.updatedAt = dt; }
}
