package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.PagedResponseDTO;
import com.rajdip.ecommerce.model.*;
import com.rajdip.ecommerce.service.PaginationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paginated")
@Tag(
    name = "Pagination & Sorting",
    description = """
        Paginated versions of all major resource lists.
        All endpoints share the same query params:

        | Param   | Default | Description                        |
        |---------|---------|------------------------------------|
        | page    | 0       | Page number (0-indexed)            |
        | size    | 10      | Items per page (max 100)           |
        | sortBy  | varies  | Field to sort by (see each endpoint)|
        | sortDir | asc     | Sort direction: asc \\| desc         |
        """
)
public class PaginationController {

    private final PaginationService paginationService;

    public PaginationController(PaginationService paginationService) {
        this.paginationService = paginationService;
    }

    // ── Products ───────────────────────────────────────────────────────────────

    @GetMapping("/products")
    @Operation(
        summary = "Products — paginated",
        description = "Paginated product catalog. **sortBy**: `name` | `price` | `id` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Product>>> getProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0")    int    page,
            @Parameter(description = "Items per page (max 100)")@RequestParam(defaultValue = "10")   int    size,
            @Parameter(description = "Sort field: name|price|id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "asc or desc")              @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(paginationService.getProductsPaged(page, size, sortBy, sortDir));
    }

    // ── Categories ─────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @Operation(
        summary = "Categories — paginated",
        description = "Paginated category list. **sortBy**: `name` | `id` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Category>>> getCategories(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {

        return ResponseEntity.ok(paginationService.getCategoriesPaged(page, size, sortBy, sortDir));
    }

    // ── Orders (admin) ─────────────────────────────────────────────────────────

    @GetMapping("/orders")
    @Operation(
        summary = "All orders — paginated (Admin)",
        description = "Admin view of all orders, paginated. **sortBy**: `id` | `status` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Order>>> getOrders(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(paginationService.getOrdersPaged(page, size, sortBy, sortDir));
    }

    // ── Orders by User ─────────────────────────────────────────────────────────

    @GetMapping("/orders/user/{userId}")
    @Operation(
        summary = "Orders by user — paginated",
        description = "Paginated order history for a specific user. **sortBy**: `id` | `status` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User orders page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Order>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ApiResponse<PagedResponseDTO<Order>> response =
                paginationService.getOrdersByUserPaged(userId, page, size, sortBy, sortDir);

        if (response.getData() == null) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    // ── Orders by Status ───────────────────────────────────────────────────────

    @GetMapping("/orders/status/{status}")
    @Operation(
        summary = "Orders by status — paginated",
        description = "Filter orders by status (PLACED/CANCELLED/REFUNDED) with pagination."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtered orders page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Order>>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(
                paginationService.getOrdersByStatusPaged(status, page, size, sortBy, sortDir));
    }

    // ── Reviews by Product ─────────────────────────────────────────────────────

    @GetMapping("/reviews/product/{productId}")
    @Operation(
        summary = "Reviews for a product — paginated",
        description = "Paginated product reviews. **sortBy**: `rating` | `id` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product reviews page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Review>>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(
                paginationService.getReviewsByProductPaged(productId, page, size, sortBy, sortDir));
    }

    // ── Reviews by User ────────────────────────────────────────────────────────

    @GetMapping("/reviews/user/{userId}")
    @Operation(
        summary = "Reviews by user — paginated",
        description = "Paginated review history for a specific user. **sortBy**: `rating` | `id` (default: `id`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User reviews page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Review>>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ApiResponse<PagedResponseDTO<Review>> response =
                paginationService.getReviewsByUserPaged(userId, page, size, sortBy, sortDir);

        if (response.getData() == null) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    // ── Payments by User ───────────────────────────────────────────────────────

    @GetMapping("/payments/user/{userId}")
    @Operation(
        summary = "Payment history by user — paginated",
        description = "Paginated payment history for a user. **sortBy**: `amount` | `createdAt` | `id` (default: `createdAt`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User payment history returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Payment>>> getPaymentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")         int    page,
            @RequestParam(defaultValue = "10")        int    size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        ApiResponse<PagedResponseDTO<Payment>> response =
                paginationService.getPaymentsByUserPaged(userId, page, size, sortBy, sortDir);

        if (response.getData() == null) return ResponseEntity.status(404).body(response);
        return ResponseEntity.ok(response);
    }

    // ── All Payments (admin) ───────────────────────────────────────────────────

    @GetMapping("/payments")
    @Operation(
        summary = "All payments — paginated (Admin)",
        description = "Admin view of all payments. **sortBy**: `amount` | `createdAt` | `status` | `id` (default: `createdAt`)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Payment>>> getPayments(
            @RequestParam(defaultValue = "0")         int    page,
            @RequestParam(defaultValue = "10")        int    size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        return ResponseEntity.ok(paginationService.getPaymentsPaged(page, size, sortBy, sortDir));
    }

    // ── Payments by Status ─────────────────────────────────────────────────────

    @GetMapping("/payments/status/{status}")
    @Operation(
        summary = "Payments by status — paginated (Admin)",
        description = "Filter payments by status (PENDING/SUCCESS/FAILED/REFUNDED) with pagination."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtered payments page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<PagedResponseDTO<Payment>>> getPaymentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0")         int    page,
            @RequestParam(defaultValue = "10")        int    size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        return ResponseEntity.ok(
                paginationService.getPaymentsByStatusPaged(status, page, size, sortBy, sortDir));
    }
}
