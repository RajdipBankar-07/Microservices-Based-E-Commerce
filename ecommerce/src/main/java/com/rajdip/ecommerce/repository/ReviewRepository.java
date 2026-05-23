package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct_Id(Long productId);

    List<Review> findByUser_Id(Long userId);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    long countByProduct_Id(Long productId);

    List<Review> findByProduct_IdAndRating(Long productId, int rating);

    List<Review> findByProduct_IdAndVerifiedPurchaseTrue(Long productId);

    // ── Dashboard queries ──────────────────────────────────────────────────────

    // Overall average rating across all products
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    double findOverallAverageRating();

    // Count verified purchase reviews
    long countByVerifiedPurchaseTrue();
}
