package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    List<Payment> findByUser_Id(Long userId);

    List<Payment> findByStatus(String status);

    boolean existsByOrder_Id(Long orderId);

    // ── Dashboard queries ──────────────────────────────────────────────────────

    // Total revenue from all SUCCESS payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    double sumSuccessPayments();

    long countByStatus(String status);

    // ── Day 8: Paginated queries ───────────────────────────────────────────────

    // User's payment history paginated
    Page<Payment> findByUser_Id(Long userId, Pageable pageable);

    // All payments paginated (admin)
    Page<Payment> findAll(Pageable pageable);

    // Payments by status paginated
    Page<Payment> findByStatus(String status, Pageable pageable);
}
