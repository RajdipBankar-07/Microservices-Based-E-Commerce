package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.PagedResponseDTO;
import com.rajdip.ecommerce.model.*;
import com.rajdip.ecommerce.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Day 8 — Pagination & Sorting Service.
 *
 * Provides paginated versions of all major resource lists:
 *  1. Products       — sortBy: name | price | id
 *  2. Categories     — sortBy: name | id
 *  3. Orders (all)   — sortBy: id | status
 *  4. Orders by user — sortBy: id | status
 *  5. Orders by status filter + paginated
 *  6. Reviews by product — sortBy: rating | id
 *  7. Reviews by user    — sortBy: rating | id
 *  8. Payments by user   — sortBy: amount | createdAt | id
 *  9. Payments (admin)   — sortBy: amount | createdAt | status | id
 * 10. Payments by status — paginated
 */
@Service
public class PaginationService {

    private static final int     DEFAULT_PAGE  = 0;
    private static final int     DEFAULT_SIZE  = 10;
    private static final int     MAX_SIZE      = 100;

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository    orderRepository;
    private final ReviewRepository   reviewRepository;
    private final PaymentRepository  paymentRepository;
    private final UserRepository     userRepository;

    public PaginationService(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             OrderRepository orderRepository,
                             ReviewRepository reviewRepository,
                             PaymentRepository paymentRepository,
                             UserRepository userRepository) {
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository    = orderRepository;
        this.reviewRepository   = reviewRepository;
        this.paymentRepository  = paymentRepository;
        this.userRepository     = userRepository;
    }

    // ── 1. Products ────────────────────────────────────────────────────────────

    /**
     * Valid sortBy: name | price | id (default: id)
     */
    public ApiResponse<PagedResponseDTO<Product>> getProductsPaged(
            int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "name", "price"), sortDir);
        Page<Product> result = productRepository.findAll(pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 2. Categories ──────────────────────────────────────────────────────────

    /**
     * Valid sortBy: name | id (default: id)
     */
    public ApiResponse<PagedResponseDTO<Category>> getCategoriesPaged(
            int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "name"), sortDir);
        Page<Category> result = categoryRepository.findAll(pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 3. All Orders (admin) ──────────────────────────────────────────────────

    /**
     * Valid sortBy: id | status (default: id)
     */
    public ApiResponse<PagedResponseDTO<Order>> getOrdersPaged(
            int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "status"), sortDir);
        Page<Order> result = orderRepository.findAll(pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 4. Orders by User ─────────────────────────────────────────────────────

    public ApiResponse<PagedResponseDTO<Order>> getOrdersByUserPaged(
            Long userId, int page, int size, String sortBy, String sortDir) {

        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "status"), sortDir);
        Page<Order> result = orderRepository.findByUser_Id(userId, pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 5. Orders by Status ───────────────────────────────────────────────────

    public ApiResponse<PagedResponseDTO<Order>> getOrdersByStatusPaged(
            String status, int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "status"), sortDir);
        Page<Order> result = orderRepository.findByStatus(status.toUpperCase(), pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 6. Reviews by Product ─────────────────────────────────────────────────

    /**
     * Valid sortBy: rating | id (default: id)
     */
    public ApiResponse<PagedResponseDTO<Review>> getReviewsByProductPaged(
            Long productId, int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "rating"), sortDir);
        Page<Review> result = reviewRepository.findByProduct_Id(productId, pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 7. Reviews by User ────────────────────────────────────────────────────

    public ApiResponse<PagedResponseDTO<Review>> getReviewsByUserPaged(
            Long userId, int page, int size, String sortBy, String sortDir) {

        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        PageRequest pr = buildPageRequest(page, size, resolveSortField(sortBy, "id", "rating"), sortDir);
        Page<Review> result = reviewRepository.findByUser_Id(userId, pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 8. Payments by User ───────────────────────────────────────────────────

    /**
     * Valid sortBy: amount | createdAt | id (default: createdAt)
     */
    public ApiResponse<PagedResponseDTO<Payment>> getPaymentsByUserPaged(
            Long userId, int page, int size, String sortBy, String sortDir) {

        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        PageRequest pr = buildPageRequest(page, size,
                resolveSortField(sortBy, "createdAt", "amount", "id"), sortDir);
        Page<Payment> result = paymentRepository.findByUser_Id(userId, pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 9. All Payments (admin) ────────────────────────────────────────────────

    public ApiResponse<PagedResponseDTO<Payment>> getPaymentsPaged(
            int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size,
                resolveSortField(sortBy, "createdAt", "amount", "status", "id"), sortDir);
        Page<Payment> result = paymentRepository.findAll(pr);
        return ok(result, sortBy, sortDir);
    }

    // ── 10. Payments by Status ────────────────────────────────────────────────

    public ApiResponse<PagedResponseDTO<Payment>> getPaymentsByStatusPaged(
            String status, int page, int size, String sortBy, String sortDir) {

        PageRequest pr = buildPageRequest(page, size,
                resolveSortField(sortBy, "createdAt", "amount", "id"), sortDir);
        Page<Payment> result = paymentRepository.findByStatus(status.toUpperCase(), pr);
        return ok(result, sortBy, sortDir);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a Spring PageRequest with validated page/size and sorted by field+dir.
     */
    private PageRequest buildPageRequest(int page, int size, String field, String sortDir) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_SIZE);
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(safePage, safeSize, sort);
    }

    /**
     * Validates sortBy against a whitelist of allowed fields.
     * Returns the default if sortBy is null or not in the whitelist.
     *
     * @param sortBy   requested sort field
     * @param defaultField first argument is always the default
     * @param allowed  additional allowed field names
     */
    private String resolveSortField(String sortBy, String defaultField, String... allowed) {
        if (sortBy == null || sortBy.isBlank()) return defaultField;
        for (String a : allowed) {
            if (a.equalsIgnoreCase(sortBy.trim())) return a;
        }
        if (defaultField.equalsIgnoreCase(sortBy.trim())) return defaultField;
        return defaultField; // fallback
    }

    /** Wraps a Spring Page into our ApiResponse<PagedResponseDTO<T>> */
    private <T> ApiResponse<PagedResponseDTO<T>> ok(Page<T> page, String sortBy, String sortDir) {
        return new ApiResponse<>(
                "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() +
                " — " + page.getTotalElements() + " total record(s)",
                new PagedResponseDTO<>(page, sortBy, sortDir)
        );
    }
}
