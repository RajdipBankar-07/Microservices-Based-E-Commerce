// src/main/java/com/rajdip/ecommerce/controller/CheckoutController.java
package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CheckoutRequest;
import com.rajdip.ecommerce.dto.CheckoutResponse;
import com.rajdip.ecommerce.dto.CouponApplyResponseDTO;
import com.rajdip.ecommerce.dto.CouponApplyRequest;
import com.rajdip.ecommerce.dto.OrderRequest;
import com.rajdip.ecommerce.model.CartItem;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.service.CouponService;
import com.rajdip.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/checkout")
@Tag(name = "Checkout", description = "Endpoints for checkout processing")
public class CheckoutController {

    private final OrderService orderService;
    private final CouponService couponService;

    public CheckoutController(OrderService orderService, CouponService couponService) {
        this.orderService = orderService;
        this.couponService = couponService;
    }

    @PostMapping
    @Operation(summary = "Process checkout", description = "Create orders from cart items, apply optional coupon, and return summary")
    @SecurityRequirement(name = "Bearer Auth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Checkout successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request / insufficient stock")

    public ResponseEntity<com.rajdip.ecommerce.dto.ApiResponse<CheckoutResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        // Basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Cart is empty", null));
        }

        // Compute total before discount
        double totalBeforeDiscount = 0.0;
        List<Long> createdOrderIds = new ArrayList<>();
        for (CartItem item : request.getItems()) {
            // Build OrderRequest for each cart item
            OrderRequest orderReq = new OrderRequest();
            orderReq.setUserId(item.getUser().getId());
            orderReq.setProductId(item.getProduct().getId());
            orderReq.setQuantity(item.getQuantity());
            // Place order (stock validation inside service)
            com.rajdip.ecommerce.dto.ApiResponse<Order> orderResp = orderService.placeOrder(orderReq);
            if (orderResp.getData() == null) {
                // Propagate error (e.g., insufficient stock)
                return ResponseEntity.badRequest().body(new ApiResponse<>(orderResp.getMessage(), null));
            }
            Order created = orderResp.getData();
            createdOrderIds.add(created.getId());
            totalBeforeDiscount += created.getProduct().getPrice() * created.getQuantity();
        }

        double discountAmount = 0.0;
        double finalTotal = totalBeforeDiscount;
        String message = "Checkout completed without coupon";

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            // Validate coupon (preview) – we use applyCoupon on a dummy order later if needed
            // For simplicity, use validateCoupon which does not persist usage
            com.rajdip.ecommerce.dto.ApiResponse<CouponApplyResponseDTO> validation =
                    couponService.validateCoupon(request.getCouponCode(), request.getItems().get(0).getUser().getId(), totalBeforeDiscount);
            if (validation.getData() != null) {
                CouponApplyResponseDTO resp = validation.getData();
                discountAmount = resp.getDiscountAmount();
                finalTotal = resp.getFinalAmount();
                message = resp.getMessage();
                // Record usage by applying to the first created order (if any)
                if (!createdOrderIds.isEmpty()) {
                    com.rajdip.ecommerce.dto.CouponApplyRequest applyReq = new com.rajdip.ecommerce.dto.CouponApplyRequest();
                    applyReq.setOrderId(createdOrderIds.get(0));
                    applyReq.setCouponCode(request.getCouponCode());
                    applyReq.setUserId(request.getItems().get(0).getUser().getId());
                    couponService.applyCoupon(applyReq);
                }
            } else {
                // Invalid coupon – continue without discount, but inform user
                message = validation.getMessage();
            }
        }

        CheckoutResponse response = new CheckoutResponse();
        response.setOrderIds(createdOrderIds);
        response.setTotalBeforeDiscount(totalBeforeDiscount);
        response.setDiscountAmount(discountAmount);
        response.setFinalTotal(finalTotal);
        response.setMessage(message);

        return ResponseEntity.ok(new ApiResponse<>("Checkout processed", response));
    }
}
