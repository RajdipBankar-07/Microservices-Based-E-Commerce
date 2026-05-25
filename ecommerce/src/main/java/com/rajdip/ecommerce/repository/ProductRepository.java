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

    // ── Day 7: Search & Filter queries ────────────────────────────────────────

    /**
     * Flexible search with all optional filters:
     *  - keyword  : matches product name (case-insensitive LIKE)
     *  - categoryId: filter by category (null = any category)
     *  - minPrice : minimum price (null = 0)
     *  - maxPrice : maximum price (null = unlimited)
     *  - inStockOnly: true = only products with quantity > 0
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN p.category c
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:inStockOnly = false OR p.quantity > 0)
        """)
    List<Product> searchProducts(
            @Param("keyword")     String  keyword,
            @Param("categoryId")  Long    categoryId,
            @Param("minPrice")    Double  minPrice,
            @Param("maxPrice")    Double  maxPrice,
            @Param("inStockOnly") boolean inStockOnly
    );

    // Name-only keyword search (quick search bar)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    // Price range filter only
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByPriceRange(
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice
    );

    // In-stock products only
    @Query("SELECT p FROM Product p WHERE p.quantity > 0 ORDER BY p.name ASC")
    List<Product> findInStockProducts();
}