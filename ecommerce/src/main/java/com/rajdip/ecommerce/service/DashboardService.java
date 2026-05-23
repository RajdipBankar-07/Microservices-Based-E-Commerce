package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.*;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD   = 5;
    private static final int RECENT_ORDERS_COUNT   = 10;
    private static final int TOP_PRODUCTS_COUNT    = 5;

    private final UserRepository    userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository   orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository  reviewRepository;
    private final CartRepository    cartRepository;

    public DashboardService(
            UserRepository userRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository,
            CartRepository cartRepository) {

        this.userRepository     = userRepository;
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository    = orderRepository;
        this.paymentRepository  = paymentRepository;
        this.reviewRepository   = reviewRepository;
        this.cartRepository     = cartRepository;
    }

    // ── Full Dashboard ─────────────────────────────────────────────────────────

    public DashboardStatsDTO getFullDashboard() {
        return new DashboardStatsDTO(
                getUserCount(),
                getProductCount(),
                getCategoryCount(),
                getActiveCartUsers(),
                getOrderStats(),
                getPaymentStats(),
                getReviewStats(),
                getLowStockProducts(),
                getOutOfStockProducts(),
                getTopSellingProducts(),
                getRecentOrders()
        );
    }

    // ── Individual stat sections ───────────────────────────────────────────────

    /** Total registered users */
    public long getUserCount() {
        return userRepository.count();
    }

    /** Total products in catalog */
    public long getProductCount() {
        return productRepository.count();
    }

    /** Total categories */
    public long getCategoryCount() {
        return categoryRepository.count();
    }

    /** Distinct users who have at least one item in their cart */
    public long getActiveCartUsers() {
        return cartRepository.findAll().stream()
                .map(item -> item.getUser().getId())
                .distinct()
                .count();
    }

    /** Order counts broken down by status */
    public OrderStatsDTO getOrderStats() {
        long total     = orderRepository.count();
        long placed    = orderRepository.countByStatus("PLACED");
        long cancelled = orderRepository.countByStatus("CANCELLED");
        long refunded  = orderRepository.countByStatus("REFUNDED");
        return new OrderStatsDTO(total, placed, cancelled, refunded);
    }

    /** Payment counts by status + total revenue */
    public PaymentStatsDTO getPaymentStats() {
        long   total    = paymentRepository.count();
        long   pending  = paymentRepository.countByStatus("PENDING");
        long   success  = paymentRepository.countByStatus("SUCCESS");
        long   failed   = paymentRepository.countByStatus("FAILED");
        long   refunded = paymentRepository.countByStatus("REFUNDED");
        double revenue  = paymentRepository.sumSuccessPayments();
        return new PaymentStatsDTO(total, pending, success, failed, refunded, revenue);
    }

    /** Platform-wide review stats */
    public ReviewStatsDTO getReviewStats() {
        long   total    = reviewRepository.count();
        double avgRaw   = reviewRepository.findOverallAverageRating();
        long   verified = reviewRepository.countByVerifiedPurchaseTrue();
        return new ReviewStatsDTO(total, avgRaw, verified);
    }

    /** Products with stock <= LOW_STOCK_THRESHOLD (default 5) */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
    }

    /** Products completely out of stock */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByQuantity(0);
    }

    /**
     * Top 5 best-selling products.
     * Returns list of maps: [{productId, productName, orderCount}]
     */
    public List<Map<String, Object>> getTopSellingProducts() {
        List<Object[]> raw = orderRepository.findTopOrderedProducts();

        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(raw.size(), TOP_PRODUCTS_COUNT);

        for (int i = 0; i < limit; i++) {
            Object[] row       = raw.get(i);
            Long productId     = (Long) row[0];
            Long orderCount    = (Long) row[1];

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank",       i + 1);
            entry.put("productId",  productId);
            entry.put("orderCount", orderCount);

            // Enrich with product name
            productRepository.findById(productId).ifPresent(p -> {
                entry.put("productName",  p.getName());
                entry.put("productPrice", p.getPrice());
                entry.put("stockLeft",    p.getQuantity());
            });

            result.add(entry);
        }
        return result;
    }

    /** Last RECENT_ORDERS_COUNT orders sorted by newest first */
    public List<Order> getRecentOrders() {
        return orderRepository.findRecentOrders(PageRequest.of(0, RECENT_ORDERS_COUNT));
    }
}
