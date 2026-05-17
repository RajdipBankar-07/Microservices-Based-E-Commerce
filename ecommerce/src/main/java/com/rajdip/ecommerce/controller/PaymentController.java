package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.PaymentRequest;
import com.rajdip.ecommerce.dto.PaymentStatusUpdateRequest;
import com.rajdip.ecommerce.model.Payment;
import com.rajdip.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(
    name = "Payment Management",
    description = "Endpoints for payment lifecycle — initiate, simulate gateway callback, view history, and refund"
)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── POST /payments ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Initiate payment",
        description = """
            Start a payment for a PLACED order.
            - Generates a unique Transaction ID.
            - CASH_ON_DELIVERY is auto-confirmed (status = SUCCESS).
            - Other methods start as PENDING — use PATCH /payments/{id}/status to confirm.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment initiated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order not PLACED, duplicate payment, or invalid method"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<Payment>> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {

        ApiResponse<Payment> response = paymentService.initiatePayment(request);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if (msg.contains("not found")) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PATCH /payments/{id}/status ────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update payment status (gateway callback simulation)",
        description = """
            Simulate a payment gateway callback.
            - Allowed transitions: PENDING → SUCCESS or PENDING → FAILED.
            - Only PENDING payments can be updated.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status or payment not PENDING"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<Payment>> updateStatus(
            @PathVariable Long id,
            @RequestBody PaymentStatusUpdateRequest body) {

        ApiResponse<Payment> response = paymentService.updateStatus(id, body.getStatus());

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Payment not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /payments/order/{orderId} ──────────────────────────────────────────

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Get payment by order ID",
        description = "Retrieve the payment record linked to a specific order."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No payment for this order")
    })
    public ResponseEntity<ApiResponse<Payment>> getByOrderId(@PathVariable Long orderId) {
        ApiResponse<Payment> response = paymentService.getByOrderId(orderId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /payments/user/{userId} ────────────────────────────────────────────

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get all payments by user",
        description = "Retrieve the full payment history for a specific user."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment history retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Payment>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getByUserId(userId));
    }

    // ── GET /payments ──────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all payments (Admin)",
        description = "Retrieve every payment in the system. Intended for admin use."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All payments retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Payment>>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    // ── GET /payments/status/{status} ──────────────────────────────────────────

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get payments by status",
        description = "Filter payments by status: PENDING, SUCCESS, FAILED, or REFUNDED."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Payment>>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.getByStatus(status));
    }

    // ── POST /payments/{id}/refund ─────────────────────────────────────────────

    @PostMapping("/{id}/refund")
    @Operation(
        summary = "Refund a payment",
        description = """
            Refund a SUCCESS payment:
            - Marks payment status → REFUNDED.
            - Automatically triggers order refund → stock is restored.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment refunded, stock restored"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment not refundable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<Payment>> refundPayment(@PathVariable Long id) {
        ApiResponse<Payment> response = paymentService.refundPayment(id);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Payment not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
