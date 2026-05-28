package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CouponApplyRequest;
import com.rajdip.ecommerce.dto.CouponApplyResponseDTO;
import com.rajdip.ecommerce.dto.CouponRequest;
import com.rajdip.ecommerce.model.*;
import com.rajdip.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Day 10 — Coupon / Discount Code Service.
 *
 * Admin operations:
 *  1. Create coupon
 *  2. Update coupon
 *  3. Toggle active/inactive
 *  4. Delete coupon
 *  5. List all / active / valid / expired
 *  6. Coupon usage stats
 *
 * User operations:
 *  7. Validate coupon (preview discount without applying)
 *  8. Apply coupon to an order (records usage, increments counter)
 *  9. Get my coupon usage history
 */
@Service
public class CouponService {

    private final CouponRepository      couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository        userRepository;
    private final OrderRepository       orderRepository;
    private final SequenceGeneratorService sequenceService;

    public CouponService(CouponRepository couponRepository,
                         CouponUsageRepository couponUsageRepository,
                         UserRepository userRepository,
                         OrderRepository orderRepository,
                         SequenceGeneratorService sequenceService) {
        this.couponRepository      = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.userRepository        = userRepository;
        this.orderRepository       = orderRepository;
        this.sequenceService       = sequenceService;
    }

    // ── 1. Create Coupon (admin) ───────────────────────────────────────────────

    @Transactional
    public ApiResponse<Coupon> createCoupon(CouponRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCode(code)) {
            return new ApiResponse<>("Coupon code '" + code + "' already exists", null);
        }

        Coupon coupon = mapFromRequest(new Coupon(), request, code);
        coupon.setId(sequenceService.nextId("coupons"));
        return new ApiResponse<>("Coupon created successfully", couponRepository.save(coupon));
    }

    // ── 2. Update Coupon (admin) ───────────────────────────────────────────────

    @Transactional
    public ApiResponse<Coupon> updateCoupon(Long id, CouponRequest request) {
        Optional<Coupon> opt = couponRepository.findById(id);
        if (opt.isEmpty()) return new ApiResponse<>("Coupon not found", null);

        Coupon coupon = opt.get();
        String newCode = request.getCode().trim().toUpperCase();

        // Code uniqueness check (allow keeping same code)
        if (!coupon.getCode().equals(newCode) && couponRepository.existsByCode(newCode)) {
            return new ApiResponse<>("Coupon code '" + newCode + "' is already taken", null);
        }

        mapFromRequest(coupon, request, newCode);
        return new ApiResponse<>("Coupon updated successfully", couponRepository.save(coupon));
    }

    // ── 3. Toggle Active ──────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<Coupon> toggleActive(Long id) {
        Optional<Coupon> opt = couponRepository.findById(id);
        if (opt.isEmpty()) return new ApiResponse<>("Coupon not found", null);

        Coupon coupon = opt.get();
        coupon.setActive(!coupon.isActive());
        Coupon saved = couponRepository.save(coupon);
        String status = saved.isActive() ? "activated" : "deactivated";
        return new ApiResponse<>("Coupon " + status + " successfully", saved);
    }

    // ── 4. Delete Coupon (admin) ───────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            return new ApiResponse<>("Coupon not found", null);
        }
        couponRepository.deleteById(id);
        return new ApiResponse<>("Coupon deleted successfully", "DELETED");
    }

    // ── 5. List all / active / valid / expired ────────────────────────────────

    public ApiResponse<List<Coupon>> getAllCoupons() {
        List<Coupon> list = couponRepository.findAll();
        return new ApiResponse<>(list.size() + " coupon(s) found", list);
    }

    public ApiResponse<List<Coupon>> getActiveCoupons() {
        List<Coupon> list = couponRepository.findByIsActiveTrue();
        return new ApiResponse<>(list.size() + " active coupon(s)", list);
    }

    public ApiResponse<List<Coupon>> getValidCoupons() {
        List<Coupon> list = couponRepository.findAllValidCoupons(LocalDate.now());
        return new ApiResponse<>(list.size() + " currently valid coupon(s)", list);
    }

    public ApiResponse<List<Coupon>> getExpiredCoupons() {
        List<Coupon> list = couponRepository.findExpiredCoupons(LocalDate.now());
        return new ApiResponse<>(list.size() + " expired coupon(s)", list);
    }

    // ── 6. Coupon usage stats (admin) ─────────────────────────────────────────

    public ApiResponse<Map<String, Object>> getCouponStats(Long couponId) {
        Optional<Coupon> opt = couponRepository.findById(couponId);
        if (opt.isEmpty()) return new ApiResponse<>("Coupon not found", null);

        Coupon coupon       = opt.get();
        List<CouponUsage> usages = couponUsageRepository.findByCoupon_Id(couponId);
        long   usageCount   = usages.size();
        double totalSavings = usages.stream().mapToDouble(CouponUsage::getDiscountAmount).sum();
        boolean isValid     = coupon.isActive()
                              && !coupon.getExpiryDate().isBefore(LocalDate.now())
                              && (coupon.getMaxUses() == 0 || coupon.getCurrentUses() < coupon.getMaxUses());

        Map<String, Object> stats = Map.of(
                "couponCode",    coupon.getCode(),
                "discountType",  coupon.getDiscountType(),
                "discountValue", coupon.getDiscountValue(),
                "totalUses",     usageCount,
                "maxUses",       coupon.getMaxUses() == 0 ? "unlimited" : coupon.getMaxUses(),
                "totalSavings",  totalSavings,
                "expiryDate",    coupon.getExpiryDate(),
                "isActive",      coupon.isActive(),
                "isCurrentlyValid", isValid
        );
        return new ApiResponse<>("Coupon stats retrieved", stats);
    }

    // ── 7. Validate coupon (preview — no side effects) ────────────────────────

    public ApiResponse<CouponApplyResponseDTO> validateCoupon(
            String code, Long userId, double orderAmount) {

        return internalValidate(code, userId, orderAmount, null, false);
    }

    // ── 8. Apply coupon to order ──────────────────────────────────────────────

    @Transactional
    public ApiResponse<CouponApplyResponseDTO> applyCoupon(CouponApplyRequest request) {

        // Resolve order
        Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            return new ApiResponse<>("Order not found", null);
        }

        Order order = orderOpt.get();
        double orderAmount = order.getProduct().getPrice() * order.getQuantity();

        return internalValidate(
                request.getCouponCode(),
                request.getUserId(),
                orderAmount,
                order,
                true   // actually record usage
        );
    }

    // ── 9. User coupon history ────────────────────────────────────────────────

    public ApiResponse<List<CouponUsage>> getUserCouponHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        List<CouponUsage> history = couponUsageRepository.findByUser_Id(userId);
        return new ApiResponse<>(history.size() + " coupon usage record(s)", history);
    }

    // ── Core validation & application logic ───────────────────────────────────

    private ApiResponse<CouponApplyResponseDTO> internalValidate(
            String rawCode, Long userId, double orderAmount,
            Order order, boolean persist) {

        String code = rawCode.trim().toUpperCase();

        // 1. Coupon exists?
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isEmpty()) {
            return new ApiResponse<>("Coupon code '" + code + "' is not valid", null);
        }
        Coupon coupon = couponOpt.get();

        // 2. Active?
        if (!coupon.isActive()) {
            return new ApiResponse<>("Coupon '" + code + "' is not active", null);
        }

        // 3. Expired?
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            return new ApiResponse<>("Coupon '" + code + "' has expired (expired on "
                    + coupon.getExpiryDate() + ")", null);
        }

        // 4. Usage limit?
        if (coupon.getMaxUses() > 0 && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            return new ApiResponse<>("Coupon '" + code + "' has reached its usage limit", null);
        }

        // 5. Already used by this user?
        if (couponUsageRepository.existsByCoupon_IdAndUser_Id(coupon.getId(), userId)) {
            return new ApiResponse<>("You have already used this coupon", null);
        }

        // 6. Min order amount?
        if (orderAmount < coupon.getMinOrderAmount()) {
            return new ApiResponse<>(
                    String.format("Minimum order amount is ₹%.2f to use this coupon (your order: ₹%.2f)",
                            coupon.getMinOrderAmount(), orderAmount), null);
        }

        // 7. Calculate discount
        double discount = calculateDiscount(coupon, orderAmount);
        double finalAmount = Math.max(0, orderAmount - discount);

        // 8. Persist (apply) if requested
        if (persist && order != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return new ApiResponse<>("User not found", null);

            // Record usage
            CouponUsage usage = new CouponUsage();
            usage.setId(sequenceService.nextId("coupon_usages"));
            usage.setCoupon(coupon);
            usage.setUser(userOpt.get());
            usage.setOrder(order);
            usage.setOriginalAmount(orderAmount);
            usage.setDiscountAmount(discount);
            usage.setFinalAmount(finalAmount);
            couponUsageRepository.save(usage);

            // Increment counter
            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepository.save(coupon);
        }

        CouponApplyResponseDTO response = new CouponApplyResponseDTO(
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                orderAmount,
                discount,
                finalAmount
        );

        String msg = persist
                ? "Coupon applied successfully! You saved ₹" + String.format("%.2f", discount)
                : "Coupon is valid! Preview: you will save ₹" + String.format("%.2f", discount);

        return new ApiResponse<>(msg, response);
    }

    /** Calculates the discount amount based on coupon type */
    private double calculateDiscount(Coupon coupon, double orderAmount) {
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            double discount = orderAmount * coupon.getDiscountValue() / 100.0;
            // Apply cap if set
            if (coupon.getMaxDiscountAmount() > 0) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
            return Math.round(discount * 100.0) / 100.0;
        } else { // FIXED
            return Math.min(coupon.getDiscountValue(), orderAmount);
        }
    }

    /** Maps request fields onto a Coupon entity */
    private Coupon mapFromRequest(Coupon coupon, CouponRequest request, String code) {
        coupon.setCode(code);
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType().toUpperCase());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(request.isActive());
        return coupon;
    }
}
