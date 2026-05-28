package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CouponApplyRequest;
import com.rajdip.ecommerce.dto.CouponApplyResponseDTO;
import com.rajdip.ecommerce.dto.CouponRequest;
import com.rajdip.ecommerce.model.Coupon;
import com.rajdip.ecommerce.model.CouponUsage;
import com.rajdip.ecommerce.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(
    name = "Coupon & Discount Codes",
    description = """
        Day 10 — Full coupon/discount code system.

        **Admin endpoints** (`/admin/coupons`): Create, update, toggle, delete, and view usage stats.
        **User endpoints** (`/coupons`): Validate (preview) and apply coupons to orders.

        Discount types:
        - **PERCENTAGE** — `discountValue`% off order total (optional `maxDiscountAmount` cap)
        - **FIXED** — flat ₹`discountValue` off order total

        Validations enforced automatically:
        - Coupon must be active and not expired
        - Usage limit must not be exceeded
        - Minimum order amount must be met
        - Each user can use any coupon only **once**
        """
)
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ════════════════════ ADMIN ENDPOINTS (/admin/coupons) ════════════════════

    @PostMapping("/admin/coupons")
    @Operation(
        summary = "Create coupon (Admin)",
        description = "Creates a new discount coupon. Code must be unique, uppercase, 3–50 chars."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@Valid @RequestBody CouponRequest request) {
        ApiResponse<Coupon> response = couponService.createCoupon(request);
        return response.getData() == null
                ? ResponseEntity.status(400).body(response)
                : ResponseEntity.ok(response);
    }

    @GetMapping("/admin/coupons")
    @Operation(summary = "List all coupons (Admin)", description = "Returns all coupons — active, inactive, expired.")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/admin/coupons/valid")
    @Operation(summary = "List currently valid coupons (Admin)",
               description = "Returns coupons that are: active + not expired + within usage limit.")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<List<Coupon>>> getValidCoupons() {
        return ResponseEntity.ok(couponService.getValidCoupons());
    }

    @GetMapping("/admin/coupons/expired")
    @Operation(summary = "List expired coupons (Admin)", description = "Returns all coupons past their expiry date.")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<List<Coupon>>> getExpiredCoupons() {
        return ResponseEntity.ok(couponService.getExpiredCoupons());
    }

    @GetMapping("/admin/coupons/{id}/stats")
    @Operation(
        summary = "Coupon usage stats (Admin)",
        description = "Returns usage count, total ₹ savings given, validity status, and remaining uses for a coupon."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stats retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCouponStats(@PathVariable Long id) {
        ApiResponse<Map<String, Object>> response = couponService.getCouponStats(id);
        return response.getData() == null
                ? ResponseEntity.status(404).body(response)
                : ResponseEntity.ok(response);
    }

    @PutMapping("/admin/coupons/{id}")
    @Operation(summary = "Update coupon (Admin)", description = "Updates all fields of an existing coupon.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(
            @PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        ApiResponse<Coupon> response = couponService.updateCoupon(id, request);
        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/coupons/{id}/toggle")
    @Operation(
        summary = "Toggle active/inactive (Admin)",
        description = "Activates an inactive coupon or deactivates an active one."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<ApiResponse<Coupon>> toggleActive(@PathVariable Long id) {
        ApiResponse<Coupon> response = couponService.toggleActive(id);
        return response.getData() == null
                ? ResponseEntity.status(404).body(response)
                : ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/coupons/{id}")
    @Operation(summary = "Delete coupon (Admin)", description = "Permanently deletes a coupon.")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteCoupon(@PathVariable Long id) {
        ApiResponse<String> response = couponService.deleteCoupon(id);
        return response.getData() == null
                ? ResponseEntity.status(404).body(response)
                : ResponseEntity.ok(response);
    }

    // ════════════════════ USER ENDPOINTS (/coupons) ═══════════════════════════

    @GetMapping("/coupons/active")
    @Operation(
        summary = "Browse active coupons",
        description = "Returns all currently active coupons. Users can browse available deals."
    )
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<List<Coupon>>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }

    @GetMapping("/coupons/validate")
    @Operation(
        summary = "Preview coupon discount",
        description = """
            **Preview** how much you'd save — does NOT apply the coupon or record usage.

            Pass: `code`, `userId`, `orderAmount` as query params.
            Use this for a 'Check Coupon' button before placing an order.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon preview returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Coupon invalid or conditions not met")
    })
    public ResponseEntity<ApiResponse<CouponApplyResponseDTO>> validateCoupon(
            @Parameter(description = "Coupon code",     required = true) @RequestParam String code,
            @Parameter(description = "User ID",         required = true) @RequestParam Long   userId,
            @Parameter(description = "Order total (₹)", required = true) @RequestParam double orderAmount) {

        ApiResponse<CouponApplyResponseDTO> response =
                couponService.validateCoupon(code, userId, orderAmount);

        return response.getData() == null
                ? ResponseEntity.status(400).body(response)
                : ResponseEntity.ok(response);
    }

    @PostMapping("/coupons/apply")
    @Operation(
        summary = "Apply coupon to an order",
        description = """
            **Applies** the coupon to a placed order — records usage, increments counter.

            Rules enforced:
            - Coupon must be active, not expired, within usage limit
            - Order amount must meet minOrderAmount
            - User can only use each coupon **once**
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coupon applied, discount shown"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid coupon or conditions not met")
    })
    public ResponseEntity<ApiResponse<CouponApplyResponseDTO>> applyCoupon(
            @Valid @RequestBody CouponApplyRequest request) {

        ApiResponse<CouponApplyResponseDTO> response = couponService.applyCoupon(request);
        return response.getData() == null
                ? ResponseEntity.status(400).body(response)
                : ResponseEntity.ok(response);
    }

    @GetMapping("/coupons/history/{userId}")
    @Operation(
        summary = "My coupon usage history",
        description = "Returns all coupons a user has applied, with original amount, discount, and final amount for each."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<CouponUsage>>> getCouponHistory(@PathVariable Long userId) {
        ApiResponse<List<CouponUsage>> response = couponService.getUserCouponHistory(userId);
        return response.getData() == null
                ? ResponseEntity.status(404).body(response)
                : ResponseEntity.ok(response);
    }
}
