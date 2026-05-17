package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by its linked order
    Optional<Payment> findByOrder_Id(Long orderId);

    // All payments made by a specific user
    List<Payment> findByUser_Id(Long userId);

    // All payments with a given status (e.g., PENDING)
    List<Payment> findByStatus(String status);

    // Check if an order already has a payment
    boolean existsByOrder_Id(Long orderId);
}
