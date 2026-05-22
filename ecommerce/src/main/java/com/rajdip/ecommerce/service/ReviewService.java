package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.ProductReviewSummaryDTO;
import com.rajdip.ecommerce.dto.ReviewRequest;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.Review;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.OrderRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.ReviewRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository  reviewRepository;
    private final UserRepository    userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository   orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository  = reviewRepository;
        this.userRepository    = userRepository;
        this.productRepository = productRepository;
        this.orderRepository   = orderRepository;
    }

    // ── 1. Create Review ───────────────────────────────────────────────────────

    /**
     * Submits a review for a product.
     *
     * Rules:
     *  - User and product must exist.
     *  - One review per user per product (unique constraint enforced here before DB).
     *  - verifiedPurchase = true if the user has ever ordered this product.
     */
    @Transactional
    public ApiResponse<Review> create(ReviewRequest request) {

        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        // Prevent duplicate review
        if (reviewRepository.existsByUser_IdAndProduct_Id(request.getUserId(), request.getProductId())) {
            return new ApiResponse<>("You have already reviewed this product. Use the update endpoint to edit it.", null);
        }

        // Auto-set verified purchase badge
        boolean verified = orderRepository.existsByUser_IdAndProduct_Id(
                request.getUserId(), request.getProductId());

        Review review = new Review();
        review.setUser(userOpt.get());
        review.setProduct(productOpt.get());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setVerifiedPurchase(verified);
        review.setCreatedAt(LocalDateTime.now());

        return new ApiResponse<>("Review submitted successfully" + (verified ? " ✓ Verified Purchase" : ""),
                reviewRepository.save(review));
    }

    // ── 2. Update Review ───────────────────────────────────────────────────────

    /**
     * Only the original reviewer can update their review.
     */
    @Transactional
    public ApiResponse<Review> update(Long reviewId, Long requestingUserId, ReviewRequest request) {

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return new ApiResponse<>("Review not found", null);
        }

        Review review = reviewOpt.get();

        // Ownership check
        if (!review.getUser().getId().equals(requestingUserId)) {
            return new ApiResponse<>("You can only edit your own reviews", null);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        return new ApiResponse<>("Review updated successfully", reviewRepository.save(review));
    }

    // ── 3. Delete Review ───────────────────────────────────────────────────────

    /**
     * Reviewer or ADMIN can delete a review.
     * isAdmin flag is passed from controller after checking Spring Security role.
     */
    @Transactional
    public ApiResponse<String> delete(Long reviewId, Long requestingUserId, boolean isAdmin) {

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return new ApiResponse<>("Review not found", null);
        }

        Review review = reviewOpt.get();

        if (!isAdmin && !review.getUser().getId().equals(requestingUserId)) {
            return new ApiResponse<>("You can only delete your own reviews", null);
        }

        reviewRepository.deleteById(reviewId);
        return new ApiResponse<>("Review deleted successfully", "success");
    }

    // ── 4. Get Review Summary for a Product ────────────────────────────────────

    /**
     * Returns all reviews + average rating + per-star breakdown for a product.
     */
    public ApiResponse<ProductReviewSummaryDTO> getProductReviews(Long productId) {

        if (!productRepository.existsById(productId)) {
            return new ApiResponse<>("Product not found", null);
        }

        List<Review> reviews   = reviewRepository.findByProduct_Id(productId);
        Double avgRaw          = reviewRepository.findAverageRatingByProductId(productId);
        double avg             = (avgRaw != null) ? avgRaw : 0.0;
        long   total           = reviewRepository.countByProduct_Id(productId);

        // Per-star counts
        long s1 = reviews.stream().filter(r -> r.getRating() == 1).count();
        long s2 = reviews.stream().filter(r -> r.getRating() == 2).count();
        long s3 = reviews.stream().filter(r -> r.getRating() == 3).count();
        long s4 = reviews.stream().filter(r -> r.getRating() == 4).count();
        long s5 = reviews.stream().filter(r -> r.getRating() == 5).count();

        ProductReviewSummaryDTO summary =
                new ProductReviewSummaryDTO(reviews, avg, total, s1, s2, s3, s4, s5);

        return new ApiResponse<>("Reviews retrieved", summary);
    }

    // ── 5. Get All Reviews by User ─────────────────────────────────────────────

    public ApiResponse<List<Review>> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        List<Review> list = reviewRepository.findByUser_Id(userId);
        return new ApiResponse<>(list.size() + " review(s) found", list);
    }

    // ── 6. Get Review by ID ────────────────────────────────────────────────────

    public ApiResponse<Review> getById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(r -> new ApiResponse<>("Review found", r))
                .orElse(new ApiResponse<>("Review not found", null));
    }

    // ── 7. Filter Reviews by Star Rating for a Product ────────────────────────

    public ApiResponse<List<Review>> getByProductAndRating(Long productId, int star) {
        if (star < 1 || star > 5) {
            return new ApiResponse<>("Star rating must be between 1 and 5", null);
        }
        if (!productRepository.existsById(productId)) {
            return new ApiResponse<>("Product not found", null);
        }
        List<Review> list = reviewRepository.findByProduct_IdAndRating(productId, star);
        return new ApiResponse<>(list.size() + " " + star + "-star review(s) found", list);
    }

    // ── 8. Get Verified Purchase Reviews for a Product ────────────────────────

    public ApiResponse<List<Review>> getVerifiedReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            return new ApiResponse<>("Product not found", null);
        }
        List<Review> list = reviewRepository.findByProduct_IdAndVerifiedPurchaseTrue(productId);
        return new ApiResponse<>(list.size() + " verified review(s) found", list);
    }
}
