package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    double findOverallAverageRating();

    long countByVerifiedPurchaseTrue();

    // ── Day 8: Paginated queries ───────────────────────────────────────────────

    // All reviews for a product paginated (sortable by rating or id)
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);

    // All reviews by a user paginated
    Page<Review> findByUser_Id(Long userId, Pageable pageable);

    // Verified reviews for a product paginated
    Page<Review> findByProduct_IdAndVerifiedPurchaseTrue(Long productId, Pageable pageable);
}
