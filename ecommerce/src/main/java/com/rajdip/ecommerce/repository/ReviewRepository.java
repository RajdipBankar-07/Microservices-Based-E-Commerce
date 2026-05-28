package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, Long> {

    List<Review>     findByProduct_Id(Long productId);
    List<Review>     findByUser_Id(Long userId);
    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);
    boolean          existsByUser_IdAndProduct_Id(Long userId, Long productId);
    long             countByVerifiedPurchaseTrue();

    // Star-rating filter
    List<Review>     findByProduct_IdAndRating(Long productId, int rating);

    // Verified only
    List<Review>     findByProduct_IdAndVerifiedPurchaseTrue(Long productId);

    // Day 8: Paginated
    Page<Review>     findByProduct_Id(Long productId, Pageable pageable);
    Page<Review>     findByUser_Id(Long userId, Pageable pageable);
    Page<Review>     findByProduct_IdAndVerifiedPurchaseTrue(Long productId, Pageable pageable);
}
