package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.ProductReviewSummaryDTO;
import com.rajdip.ecommerce.dto.ReviewRequest;
import com.rajdip.ecommerce.model.Review;
import com.rajdip.ecommerce.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(
    name = "Product Reviews & Ratings",
    description = "Endpoints for submitting, viewing, updating, and deleting product reviews with star ratings"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ── POST /reviews ──────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Submit a review",
        description = """
            Submit a star rating (1–5) and optional comment for a product.
            - One review per user per product.
            - ✓ Verified Purchase badge is auto-set if the user has ordered the product.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review submitted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Already reviewed or invalid rating"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found")
    })
    public ResponseEntity<ApiResponse<Review>> create(@Valid @RequestBody ReviewRequest request) {
        ApiResponse<Review> response = reviewService.create(request);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if (msg.contains("not found")) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /reviews/product/{productId} ──────────────────────────────────────

    @GetMapping("/product/{productId}")
    @Operation(
        summary = "Get all reviews for a product",
        description = """
            Returns reviews list + average rating + total count + per-star breakdown (1★–5★).
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductReviewSummaryDTO>> getProductReviews(
            @PathVariable Long productId) {

        ApiResponse<ProductReviewSummaryDTO> response = reviewService.getProductReviews(productId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /reviews/product/{productId}/filter ────────────────────────────────

    @GetMapping("/product/{productId}/filter")
    @Operation(
        summary = "Filter reviews by star rating",
        description = "Get only reviews of a specific star rating (1–5) for a product."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtered reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid star value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<List<Review>>> filterByStar(
            @PathVariable Long productId,
            @RequestParam int star) {

        ApiResponse<List<Review>> response = reviewService.getByProductAndRating(productId, star);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if (msg.contains("not found")) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /reviews/product/{productId}/verified ──────────────────────────────

    @GetMapping("/product/{productId}/verified")
    @Operation(
        summary = "Get verified purchase reviews",
        description = "Retrieve only reviews from users who have actually purchased the product."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verified reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<List<Review>>> getVerified(@PathVariable Long productId) {
        ApiResponse<List<Review>> response = reviewService.getVerifiedReviews(productId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /reviews/user/{userId} ─────────────────────────────────────────────

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get all reviews by a user",
        description = "Retrieve the full review history written by a specific user."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<Review>>> getByUser(@PathVariable Long userId) {
        ApiResponse<List<Review>> response = reviewService.getByUser(userId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /reviews/{id} ─────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
        summary = "Get review by ID",
        description = "Retrieve a single review by its ID."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<Review>> getById(@PathVariable Long id) {
        ApiResponse<Review> response = reviewService.getById(id);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PUT /reviews/{id} ─────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
        summary = "Update your review",
        description = "Edit rating and/or comment of your own review. Only the original author can update."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not the review owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<Review>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {

        ApiResponse<Review> response = reviewService.update(id, request.getUserId(), request);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Review not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── DELETE /reviews/{id} ──────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a review",
        description = "Delete your own review. ADMIN can delete any review."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Not the review owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        ApiResponse<String> response = reviewService.delete(id, userId, isAdmin);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Review not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
