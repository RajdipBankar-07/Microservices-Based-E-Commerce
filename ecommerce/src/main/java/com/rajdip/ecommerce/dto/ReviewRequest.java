package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.*;

/**
 * Request body for creating or updating a review.
 */
public class ReviewRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment; // optional

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getUserId()                { return userId; }
    public void setUserId(Long userId)     { this.userId = userId; }

    public Long getProductId()                   { return productId; }
    public void setProductId(Long productId)     { this.productId = productId; }

    public int getRating()                 { return rating; }
    public void setRating(int rating)      { this.rating = rating; }

    public String getComment()             { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
