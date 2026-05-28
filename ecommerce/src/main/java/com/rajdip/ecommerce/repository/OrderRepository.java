package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, Long> {

    List<Order>  findByUser_Id(Long userId);
    boolean      existsByUser_IdAndProduct_Id(Long userId, Long productId);
    long         countByStatus(String status);

    // Day 8: Paginated
    Page<Order>  findAll(Pageable pageable);
    Page<Order>  findByUser_Id(Long userId, Pageable pageable);
    Page<Order>  findByStatus(String status, Pageable pageable);
}