package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.ProductSearchRequest;
import com.rajdip.ecommerce.dto.ProductSearchResultDTO;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Day 7 — Product Search & Filter Service.
 *
 * Handles:
 *  1. Combined search  : keyword + category + price range + stock + sort
 *  2. Quick keyword    : name contains (case-insensitive)
 *  3. Price range      : minPrice to maxPrice
 *  4. In-stock only    : quantity > 0
 *  5. Sorting          : by name / price / id, asc or desc
 */
@Service
public class ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ── 1. Full combined search ────────────────────────────────────────────────

    /**
     * Runs the flexible JPQL query with all optional filters,
     * then applies in-memory sorting since JPA dynamic sort is complex.
     */
    public ApiResponse<ProductSearchResultDTO> search(ProductSearchRequest request) {

        // Sanitise: blank keyword → null (so JPQL skips the filter)
        String keyword = (request.getKeyword() != null && !request.getKeyword().isBlank())
                ? request.getKeyword().trim()
                : null;

        List<Product> results = productRepository.searchProducts(
                keyword,
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.isInStockOnly()
        );

        // Apply sorting
        results = sort(results, request.getSortBy(), request.getSortDir());

        ProductSearchResultDTO dto = new ProductSearchResultDTO(
                results,
                keyword,
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.isInStockOnly(),
                request.getSortBy(),
                request.getSortDir()
        );

        return new ApiResponse<>(results.size() + " product(s) found", dto);
    }

    // ── 2. Quick keyword search ────────────────────────────────────────────────

    public ApiResponse<List<Product>> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ApiResponse<>("Keyword is required", null);
        }
        List<Product> results = productRepository.findByNameContainingIgnoreCase(keyword.trim());
        return new ApiResponse<>(results.size() + " product(s) found for \"" + keyword + "\"", results);
    }

    // ── 3. Price range filter ──────────────────────────────────────────────────

    public ApiResponse<List<Product>> searchByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0) {
            return new ApiResponse<>("Price values must be non-negative", null);
        }
        if (minPrice > maxPrice) {
            return new ApiResponse<>("minPrice cannot be greater than maxPrice", null);
        }
        List<Product> results = productRepository.findByPriceRange(minPrice, maxPrice);
        return new ApiResponse<>(results.size() + " product(s) in price range ₹" +
                minPrice + " – ₹" + maxPrice, results);
    }

    // ── 4. In-stock products ───────────────────────────────────────────────────

    public ApiResponse<List<Product>> getInStockProducts() {
        List<Product> results = productRepository.findInStockProducts();
        return new ApiResponse<>(results.size() + " in-stock product(s) available", results);
    }

    // ── 5. In-memory sort ─────────────────────────────────────────────────────

    /**
     * Sorts a product list by the given field and direction.
     * Supported sortBy: "name" | "price" | "id"
     * Supported sortDir: "asc" | "desc"
     */
    private List<Product> sort(List<Product> products, String sortBy, String sortDir) {
        Comparator<Product> comparator = switch (sortBy == null ? "id" : sortBy.toLowerCase()) {
            case "name"  -> Comparator.comparing(p -> p.getName().toLowerCase());
            case "price" -> Comparator.comparingDouble(Product::getPrice);
            default      -> Comparator.comparingLong(Product::getId);
        };

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        return products.stream().sorted(comparator).toList();
    }
}
