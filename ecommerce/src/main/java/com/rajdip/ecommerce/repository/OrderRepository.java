package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_Id(Long userId);

    // Used by review service to check verified purchase badge
    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    // ── Dashboard queries ──────────────────────────────────────────────────────

    // Count orders grouped by status
    long countByStatus(String status);

    // Top 5 most-ordered products (productId, orderCount)
    @Query("SELECT o.product.id, COUNT(o) AS cnt FROM Order o GROUP BY o.product.id ORDER BY cnt DESC")
    List<Object[]> findTopOrderedProducts();

    // Recent N orders (latest first)
    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);
}