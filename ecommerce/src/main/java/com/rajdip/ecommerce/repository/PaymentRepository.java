package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);
    List<Payment>     findByUser_Id(Long userId);
    boolean           existsByOrder_Id(Long orderId);
    long              countByStatus(String status);
    List<Payment>     findByStatus(String status);

    // Day 8: Paginated
    Page<Payment>     findAll(Pageable pageable);
    Page<Payment>     findByUser_Id(Long userId, Pageable pageable);
    Page<Payment>     findByStatus(String status, Pageable pageable);
}
