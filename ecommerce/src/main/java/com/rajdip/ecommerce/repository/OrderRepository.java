package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_Id(Long userId);

    // Used by review service to check verified purchase badge
    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    // ── Dashboard queries ──────────────────────────────────────────────────────

    long countByStatus(String status);

    @Query("SELECT o.product.id, COUNT(o) AS cnt FROM Order o GROUP BY o.product.id ORDER BY cnt DESC")
    List<Object[]> findTopOrderedProducts();

    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);

    // ── Day 8: Paginated queries ───────────────────────────────────────────────

    // All orders paginated (admin)
    Page<Order> findAll(Pageable pageable);

    // Orders for a specific user paginated
    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    // Orders by status paginated
    Page<Order> findByStatus(String status, Pageable pageable);
}