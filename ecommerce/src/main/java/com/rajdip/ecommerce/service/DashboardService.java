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
        
        // Calculate success payment revenue
        double revenue = paymentRepository.findAll().stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(p -> p.getAmount())
                .sum();
                
        return new PaymentStatsDTO(total, pending, success, failed, refunded, revenue);
    }

    /** Platform-wide review stats */
    public ReviewStatsDTO getReviewStats() {
        long   total    = reviewRepository.count();
        double avgRaw   = reviewRepository.findAll().stream()
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0.0);
        long   verified = reviewRepository.countByVerifiedPurchaseTrue();
        return new ReviewStatsDTO(total, avgRaw, verified);
    }

    /** Products with stock <= LOW_STOCK_THRESHOLD (default 5) */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
    }

    /** Products completely out of stock */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    /**
     * Top 5 best-selling products.
     */
    public List<Map<String, Object>> getTopSellingProducts() {
        List<Order> allOrders = orderRepository.findAll();
        
        // Count orders per product
        Map<Long, Long> counts = new HashMap<>();
        for (Order o : allOrders) {
            if (o.getProduct() != null) {
                Long pId = o.getProduct().getId();
                counts.put(pId, counts.getOrDefault(pId, 0L) + 1);
            }
        }
        
        // Sort descending by count
        List<Map.Entry<Long, Long>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(sorted.size(), TOP_PRODUCTS_COUNT);
        
        for (int i = 0; i < limit; i++) {
            Map.Entry<Long, Long> entry = sorted.get(i);
            Long productId  = entry.getKey();
            Long orderCount = entry.getValue();
            
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("rank",       i + 1);
            map.put("productId",  productId);
            map.put("orderCount", orderCount);
            
            productRepository.findById(productId).ifPresent(p -> {
                map.put("productName",  p.getName());
                map.put("productPrice", p.getPrice());
                map.put("stockLeft",    p.getQuantity());
            });
            
            result.add(map);
        }
        return result;
    }

    /** Last RECENT_ORDERS_COUNT orders sorted by newest first */
    public List<Order> getRecentOrders() {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id");
        return orderRepository.findAll(PageRequest.of(0, RECENT_ORDERS_COUNT, sort)).getContent();
    }
}
