package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews for a product
    List<Review> findByProduct_Id(Long productId);

    // All reviews written by a user
    List<Review> findByUser_Id(Long userId);

    // Specific review by a user for a product (duplicate check)
    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    // Check if user already reviewed this product
    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    // Average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Count of reviews for a product
    long countByProduct_Id(Long productId);

    // Reviews filtered by rating for a product
    List<Review> findByProduct_IdAndRating(Long productId, int rating);

    // Verified purchase reviews only for a product
    List<Review> findByProduct_IdAndVerifiedPurchaseTrue(Long productId);
}
