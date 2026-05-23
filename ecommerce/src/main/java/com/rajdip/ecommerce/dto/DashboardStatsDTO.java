package com.rajdip.ecommerce.dto;

import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.model.Product;

import java.util.List;
import java.util.Map;

/**
 * Full Admin Dashboard response — aggregates all platform statistics.
 *
 * Sections:
 *  ├── Overview   : users, products, categories, activeCartUsers
 *  ├── orders     : OrderStatsDTO (total + per-status breakdown)
 *  ├── payments   : PaymentStatsDTO (revenue + per-status breakdown)
 *  ├── reviews    : ReviewStatsDTO (avg rating, verified count)
 *  ├── inventory  : lowStockProducts + outOfStockProducts
 *  ├── topProducts: top 5 best-selling products with order count
 *  └── recentOrders: last 10 orders
 */
public class DashboardStatsDTO {

    // ── Overview ───────────────────────────────────────────────────────────────
    private long totalUsers;
    private long totalProducts;
    private long totalCategories;
    private long activeCartUsers;   // distinct users with items in cart

    // ── Breakdown DTOs ────────────────────────────────────────────────────────
    private OrderStatsDTO   orders;
    private PaymentStatsDTO payments;
    private ReviewStatsDTO  reviews;

    // ── Inventory ─────────────────────────────────────────────────────────────
    private List<Product> lowStockProducts;     // quantity <= 5
    private List<Product> outOfStockProducts;   // quantity == 0

    // ── Top sellers ───────────────────────────────────────────────────────────
    private List<Map<String, Object>> topSellingProducts; // [{productId, productName, orderCount}]

    // ── Recent activity ───────────────────────────────────────────────────────
    private List<Order> recentOrders;  // last 10 orders

    // ── Constructor ───────────────────────────────────────────────────────────

    public DashboardStatsDTO(
            long totalUsers, long totalProducts, long totalCategories, long activeCartUsers,
            OrderStatsDTO orders, PaymentStatsDTO payments, ReviewStatsDTO reviews,
            List<Product> lowStockProducts, List<Product> outOfStockProducts,
            List<Map<String, Object>> topSellingProducts, List<Order> recentOrders) {

        this.totalUsers          = totalUsers;
        this.totalProducts       = totalProducts;
        this.totalCategories     = totalCategories;
        this.activeCartUsers     = activeCartUsers;
        this.orders              = orders;
        this.payments            = payments;
        this.reviews             = reviews;
        this.lowStockProducts    = lowStockProducts;
        this.outOfStockProducts  = outOfStockProducts;
        this.topSellingProducts  = topSellingProducts;
        this.recentOrders        = recentOrders;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public long getTotalUsers()                        { return totalUsers; }
    public long getTotalProducts()                     { return totalProducts; }
    public long getTotalCategories()                   { return totalCategories; }
    public long getActiveCartUsers()                   { return activeCartUsers; }
    public OrderStatsDTO getOrders()                   { return orders; }
    public PaymentStatsDTO getPayments()               { return payments; }
    public ReviewStatsDTO getReviews()                 { return reviews; }
    public List<Product> getLowStockProducts()         { return lowStockProducts; }
    public List<Product> getOutOfStockProducts()       { return outOfStockProducts; }
    public List<Map<String, Object>> getTopSellingProducts() { return topSellingProducts; }
    public List<Order> getRecentOrders()               { return recentOrders; }
}
