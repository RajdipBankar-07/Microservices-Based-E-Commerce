package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@CompoundIndex(name = "uq_user_product_review", def = "{'user.$id': 1, 'product.$id': 1}", unique = true)
public class Review {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"password"})
    private User user;

    @DBRef
    @JsonIgnoreProperties({"category"})
    private Product product;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    private boolean verifiedPurchase = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long          getId()                 { return id; }
    public User          getUser()               { return user; }
    public Product       getProduct()            { return product; }
    public int           getRating()             { return rating; }
    public String        getComment()            { return comment; }
    public boolean       isVerifiedPurchase()    { return verifiedPurchase; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }

    public void setId(Long id)                   { this.id = id; }
    public void setUser(User u)                  { this.user = u; }
    public void setProduct(Product p)            { this.product = p; }
    public void setRating(int r)                 { this.rating = r; }
    public void setComment(String c)             { this.comment = c; }
    public void setVerifiedPurchase(boolean v)   { this.verifiedPurchase = v; }
    public void setCreatedAt(LocalDateTime dt)   { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt)   { this.updatedAt = dt; }
}
