package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Payment;
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

    // Count payments grouped by status
    long countByStatus(String status);
}
