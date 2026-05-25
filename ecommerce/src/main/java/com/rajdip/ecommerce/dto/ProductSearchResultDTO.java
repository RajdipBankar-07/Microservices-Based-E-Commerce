package com.rajdip.ecommerce.dto;

import com.rajdip.ecommerce.model.Product;

import java.util.List;

/**
 * Response wrapper for product search results.
 * Includes the result list + metadata about the query applied.
 */
public class ProductSearchResultDTO {

    private List<Product> products;
    private long          totalResults;

    // Applied filter echo (so the client knows what was used)
    private String  appliedKeyword;
    private Long    appliedCategoryId;
    private Double  appliedMinPrice;
    private Double  appliedMaxPrice;
    private boolean appliedInStockOnly;
    private String  appliedSortBy;
    private String  appliedSortDir;

    public ProductSearchResultDTO(List<Product> products,
                                  String keyword, Long categoryId,
                                  Double minPrice, Double maxPrice,
                                  boolean inStockOnly,
                                  String sortBy, String sortDir) {
        this.products           = products;
        this.totalResults       = products.size();
        this.appliedKeyword     = keyword;
        this.appliedCategoryId  = categoryId;
        this.appliedMinPrice    = minPrice;
        this.appliedMaxPrice    = maxPrice;
        this.appliedInStockOnly = inStockOnly;
        this.appliedSortBy      = sortBy;
        this.appliedSortDir     = sortDir;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<Product> getProducts()        { return products; }
    public long          getTotalResults()    { return totalResults; }
    public String        getAppliedKeyword()  { return appliedKeyword; }
    public Long          getAppliedCategoryId()  { return appliedCategoryId; }
    public Double        getAppliedMinPrice() { return appliedMinPrice; }
    public Double        getAppliedMaxPrice() { return appliedMaxPrice; }
    public boolean       isAppliedInStockOnly() { return appliedInStockOnly; }
    public String        getAppliedSortBy()   { return appliedSortBy; }
    public String        getAppliedSortDir()  { return appliedSortDir; }
}
