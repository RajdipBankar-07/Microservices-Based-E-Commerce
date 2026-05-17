package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.PaymentRequest;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.model.Payment;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.OrderRepository;
import com.rajdip.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Set<String> VALID_METHODS = Set.of(
            "CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING", "CASH_ON_DELIVERY"
    );

    private final PaymentRepository paymentRepository;
    private final OrderRepository   orderRepository;
    private final OrderService      orderService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository   = orderRepository;
        this.orderService      = orderService;
    }

    // ── 1. Initiate Payment ────────────────────────────────────────────────────

    /**
     * Creates a PENDING payment for a PLACED order.
     *
     * Rules:
     *  - Order must exist and be in PLACED status.
     *  - Order must not already have a payment.
     *  - Amount = product.price × order.quantity (captured at initiation time).
     *  - A unique transactionId is generated immediately.
     *  - CASH_ON_DELIVERY payments are auto-marked SUCCESS.
     */
    @Transactional
    public ApiResponse<Payment> initiatePayment(PaymentRequest request) {

        // Validate payment method
        if (!VALID_METHODS.contains(request.getPaymentMethod())) {
            return new ApiResponse<>("Invalid payment method. Use: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, CASH_ON_DELIVERY", null);
        }

        // Fetch order
        Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            return new ApiResponse<>("Order not found", null);
        }

        Order order = orderOpt.get();

        // Order must be PLACED
        if (!"PLACED".equals(order.getStatus())) {
            return new ApiResponse<>("Payment can only be initiated for PLACED orders. Current status: " + order.getStatus(), null);
        }

        // Prevent duplicate payment
        if (paymentRepository.existsByOrder_Id(order.getId())) {
            return new ApiResponse<>("Payment already exists for this order", null);
        }

        // Calculate amount
        double amount = order.getProduct().getPrice() * order.getQuantity();

        // Build payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(order.getUser());
        payment.setAmount(amount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId(generateTransactionId(order.getId()));
        payment.setCreatedAt(LocalDateTime.now());

        // COD is instantly successful
        if ("CASH_ON_DELIVERY".equals(request.getPaymentMethod())) {
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
        } else {
            payment.setStatus("PENDING");
        }

        Payment saved = paymentRepository.save(payment);
        String msg = "CASH_ON_DELIVERY".equals(request.getPaymentMethod())
                ? "Payment initiated and confirmed (Cash on Delivery)"
                : "Payment initiated successfully. Status: PENDING";

        return new ApiResponse<>(msg, saved);
    }

    // ── 2. Update Payment Status (simulate gateway callback) ──────────────────

    /**
     * Simulates a payment gateway callback to mark payment SUCCESS or FAILED.
     *
     * Rules:
     *  - Only PENDING payments can be updated.
     *  - Allowed new statuses: SUCCESS, FAILED.
     */
    @Transactional
    public ApiResponse<Payment> updateStatus(Long paymentId, String newStatus) {

        if (!"SUCCESS".equals(newStatus) && !"FAILED".equals(newStatus)) {
            return new ApiResponse<>("Invalid status. Allowed values: SUCCESS, FAILED", null);
        }

        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return new ApiResponse<>("Payment not found", null);
        }

        Payment payment = paymentOpt.get();

        if (!"PENDING".equals(payment.getStatus())) {
            return new ApiResponse<>("Only PENDING payments can be updated. Current status: " + payment.getStatus(), null);
        }

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

        return new ApiResponse<>("Payment status updated to " + newStatus, paymentRepository.save(payment));
    }

    // ── 3. Get Payment by Order ID ─────────────────────────────────────────────

    public ApiResponse<Payment> getByOrderId(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId)
                .map(p -> new ApiResponse<>("Payment found", p))
                .orElse(new ApiResponse<>("No payment found for order #" + orderId, null));
    }

    // ── 4. Get All Payments by User ───────────────────────────────────────────

    public ApiResponse<List<Payment>> getByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByUser_Id(userId);
        return new ApiResponse<>("Payments retrieved", payments);
    }

    // ── 5. Get All Payments (Admin) ───────────────────────────────────────────

    public ApiResponse<List<Payment>> getAll() {
        return new ApiResponse<>("All payments retrieved", paymentRepository.findAll());
    }

    // ── 6. Get by Status ──────────────────────────────────────────────────────

    public ApiResponse<List<Payment>> getByStatus(String status) {
        List<Payment> list = paymentRepository.findByStatus(status.toUpperCase());
        return new ApiResponse<>("Payments with status " + status.toUpperCase() + " retrieved", list);
    }

    // ── 7. Refund Payment ──────────────────────────────────────────────────────

    /**
     * Refunds a SUCCESS payment:
     *  1. Marks Payment → REFUNDED
     *  2. Triggers OrderService.refundOrder() → restores stock & marks Order → REFUNDED
     */
    @Transactional
    public ApiResponse<Payment> refundPayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return new ApiResponse<>("Payment not found", null);
        }

        Payment payment = paymentOpt.get();

        if ("REFUNDED".equals(payment.getStatus())) {
            return new ApiResponse<>("Payment already refunded", null);
        }
        if (!"SUCCESS".equals(payment.getStatus())) {
            return new ApiResponse<>("Only successful payments can be refunded. Current status: " + payment.getStatus(), null);
        }

        // Refund the linked order (restores stock)
        ApiResponse<Order> orderRefund = orderService.refundOrder(payment.getOrder().getId());
        if (orderRefund.getData() == null) {
            return new ApiResponse<>("Order refund failed: " + orderRefund.getMessage(), null);
        }

        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());

        return new ApiResponse<>("Payment refunded successfully. Stock restored.", paymentRepository.save(payment));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String generateTransactionId(Long orderId) {
        // Format: TXN-<orderId>-<shortUUID>
        String shortUUID = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TXN-" + orderId + "-" + shortUUID;
    }
}
