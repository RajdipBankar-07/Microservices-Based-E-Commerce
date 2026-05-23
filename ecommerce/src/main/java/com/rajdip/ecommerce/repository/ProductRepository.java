package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // All products under a specific category
    List<Product> findByCategory_Id(Long categoryId);

    // Products with no category assigned
    List<Product> findByCategoryIsNull();

    // ── Dashboard queries ──────────────────────────────────────────────────────

    // Low-stock products: quantity <= threshold
    @Query("SELECT p FROM Product p WHERE p.quantity <= :threshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    // Out-of-stock products
    List<Product> findByQuantity(int quantity);
}