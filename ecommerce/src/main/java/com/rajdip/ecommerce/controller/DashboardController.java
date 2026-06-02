package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.*;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(
    name = "Admin Dashboard",
    description = "Admin-only endpoints for platform statistics — overview, revenue, inventory alerts, top sellers, and recent orders"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ── GET /admin/dashboard ───────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Full dashboard stats",
        description = """
            Returns a complete admin dashboard snapshot:
            - Total users, products, categories, active cart users
            - Order breakdown (PLACED / CANCELLED / REFUNDED)
            - Payment breakdown (PENDING / SUCCESS / FAILED / REFUNDED) + total revenue
            - Platform-wide review stats (avg rating, verified count)
            - Low-stock & out-of-stock product alerts
            - Top 5 best-selling products
            - Last 10 orders
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard data retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getFullDashboard() {
        return ResponseEntity.ok(
                new ApiResponse<>("Dashboard retrieved successfully", dashboardService.getFullDashboard())
        );
    }

    // ── GET /admin/dashboard/orders ────────────────────────────────────────────

    @GetMapping("/orders")
    @Operation(summary = "Order statistics", description = "Total orders with PLACED / CANCELLED / REFUNDED breakdown.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order stats retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrderStats() {
        return ResponseEntity.ok(
                new ApiResponse<>("Order statistics retrieved", dashboardService.getOrderStats())
        );
    }

    // ── GET /admin/dashboard/payments ─────────────────────────────────────────

    @GetMapping("/payments")
    @Operation(summary = "Payment statistics", description = "Payment counts by status + total revenue from SUCCESS payments.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment stats retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<PaymentStatsDTO>> getPaymentStats() {
        return ResponseEntity.ok(
                new ApiResponse<>("Payment statistics retrieved", dashboardService.getPaymentStats())
        );
    }

    // ── GET /admin/dashboard/reviews ──────────────────────────────────────────

    @GetMapping("/reviews")
    @Operation(summary = "Review statistics", description = "Platform-wide review count, average star rating, and verified purchase count.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review stats retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<ReviewStatsDTO>> getReviewStats() {
        return ResponseEntity.ok(
                new ApiResponse<>("Review statistics retrieved", dashboardService.getReviewStats())
        );
    }

    // ── GET /admin/dashboard/inventory/low-stock ───────────────────────────────

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Low stock alert", description = "Products with quantity ≤ 5. Ideal for restocking decisions.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Low-stock products retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<List<Product>>> getLowStock() {
        List<Product> products = dashboardService.getLowStockProducts();
        return ResponseEntity.ok(
                new ApiResponse<>(products.size() + " low-stock product(s) found", products)
        );
    }

    // ── GET /admin/dashboard/inventory/out-of-stock ────────────────────────────

    @GetMapping("/inventory/out-of-stock")
    @Operation(summary = "Out-of-stock products", description = "Products with zero quantity — need immediate restocking.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Out-of-stock products retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<List<Product>>> getOutOfStock() {
        List<Product> products = dashboardService.getOutOfStockProducts();
        return ResponseEntity.ok(
                new ApiResponse<>(products.size() + " out-of-stock product(s) found", products)
        );
    }

    // ── GET /admin/dashboard/top-products ─────────────────────────────────────

    @GetMapping("/top-products")
    @Operation(summary = "Top 5 best-selling products", description = "Ranked by total order count. Includes product name, price, and remaining stock.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top products retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopProducts() {
        return ResponseEntity.ok(
                new ApiResponse<>("Top selling products retrieved", dashboardService.getTopSellingProducts())
        );
    }

    // ── GET /admin/dashboard/recent-orders ────────────────────────────────────

    @GetMapping("/recent-orders")
    @Operation(summary = "Recent orders", description = "Last 10 orders placed on the platform, newest first.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent orders retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<List<Order>>> getRecentOrders() {
        return ResponseEntity.ok(
                new ApiResponse<>("Recent orders retrieved", dashboardService.getRecentOrders())
        );
    }

    // ── GET /admin/dashboard/sales-report ──────────────────────────────────────

    @GetMapping("/sales-report")
    @Operation(summary = "Sales report charts data", description = "Aggregated sales data by day, week, month, and year for graphs.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sales report data retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<SalesDashboardData>> getSalesReport() {
        return ResponseEntity.ok(
                new ApiResponse<>("Sales report data retrieved", dashboardService.getSalesReport())
        );
    }
}
