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

    public SalesDashboardData getSalesReport() {
        List<Order> allOrders = orderRepository.findAll();
        
        // Filter out CANCELLED and REFUNDED orders for actual sales reporting
        List<Order> validOrders = allOrders.stream()
                .filter(o -> o.getStatus() != null && 
                            !"CANCELLED".equalsIgnoreCase(o.getStatus()) && 
                            !"REFUNDED".equalsIgnoreCase(o.getStatus()))
                .toList();

        // 1. Day stats: Today's orders grouped by hour of day (0-23)
        List<SalesChartPoint> dayPoints = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int hour = 0; hour < 24; hour++) {
            final int h = hour;
            String label = String.format("%02d:00", h);
            List<Order> hourOrders = validOrders.stream()
                    .filter(o -> o.getOrderDate() != null && 
                                o.getOrderDate().toLocalDate().equals(today) && 
                                o.getOrderDate().getHour() == h)
                    .toList();
            double rev = hourOrders.stream().mapToDouble(o -> o.getQuantity() * (o.getProduct() != null ? o.getProduct().getPrice() : 0)).sum();
            dayPoints.add(new SalesChartPoint(label, rev, hourOrders.size()));
        }

        // 2. Week stats: Last 7 days including today
        List<SalesChartPoint> weekPoints = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            String label = d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
            List<Order> dayOrders = validOrders.stream()
                    .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().equals(d))
                    .toList();
            double rev = dayOrders.stream().mapToDouble(o -> o.getQuantity() * (o.getProduct() != null ? o.getProduct().getPrice() : 0)).sum();
            weekPoints.add(new SalesChartPoint(label, rev, dayOrders.size()));
        }

        // 3. Month stats: Days of the current month
        List<SalesChartPoint> monthPoints = new ArrayList<>();
        java.time.YearMonth currentYearMonth = java.time.YearMonth.now();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            final int dNum = day;
            String label = String.format("%d", dNum);
            List<Order> dayOrders = validOrders.stream()
                    .filter(o -> o.getOrderDate() != null && 
                                o.getOrderDate().getYear() == currentYearMonth.getYear() && 
                                o.getOrderDate().getMonthValue() == currentYearMonth.getMonthValue() && 
                                o.getOrderDate().getDayOfMonth() == dNum)
                    .toList();
            double rev = dayOrders.stream().mapToDouble(o -> o.getQuantity() * (o.getProduct() != null ? o.getProduct().getPrice() : 0)).sum();
            monthPoints.add(new SalesChartPoint(label, rev, dayOrders.size()));
        }

        // 4. Year stats: Months of the current year (Jan-Dec)
        List<SalesChartPoint> yearPoints = new ArrayList<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int currentYear = today.getYear();
        for (int monthVal = 1; monthVal <= 12; monthVal++) {
            final int mNum = monthVal;
            String label = monthNames[mNum - 1];
            List<Order> monthOrders = validOrders.stream()
                    .filter(o -> o.getOrderDate() != null && 
                                o.getOrderDate().getYear() == currentYear && 
                                o.getOrderDate().getMonthValue() == mNum)
                    .toList();
            double rev = monthOrders.stream().mapToDouble(o -> o.getQuantity() * (o.getProduct() != null ? o.getProduct().getPrice() : 0)).sum();
            yearPoints.add(new SalesChartPoint(label, rev, monthOrders.size()));
        }

        return new SalesDashboardData(dayPoints, weekPoints, monthPoints, yearPoints);
    }
}
