package com.rajdip.ecommerce.dto;

/**
 * Query parameters for the product search endpoint.
 *
 * All fields are optional — omitting one disables that filter.
 *
 * Examples:
 *   ?keyword=phone
 *   ?minPrice=100&maxPrice=500
 *   ?categoryId=2&inStockOnly=true
 *   ?keyword=shirt&categoryId=3&minPrice=200&maxPrice=2000&inStockOnly=true&sortBy=price&sortDir=asc
 */
public class ProductSearchRequest {

    private String  keyword;       // name contains (case-insensitive)
    private Long    categoryId;    // exact category match
    private Double  minPrice;      // price >= minPrice
    private Double  maxPrice;      // price <= maxPrice
    private boolean inStockOnly;   // quantity > 0 only

    /**
     * Sort field: "name" | "price" | "id" (default: "id")
     */
    private String sortBy = "id";

    /**
     * Sort direction: "asc" | "desc" (default: "asc")
     */
    private String sortDir = "asc";

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String  getKeyword()             { return keyword; }
    public void    setKeyword(String k)     { this.keyword = k; }

    public Long    getCategoryId()          { return categoryId; }
    public void    setCategoryId(Long id)   { this.categoryId = id; }

    public Double  getMinPrice()            { return minPrice; }
    public void    setMinPrice(Double p)    { this.minPrice = p; }

    public Double  getMaxPrice()            { return maxPrice; }
    public void    setMaxPrice(Double p)    { this.maxPrice = p; }

    public boolean isInStockOnly()               { return inStockOnly; }
    public void    setInStockOnly(boolean b)     { this.inStockOnly = b; }

    public String  getSortBy()              { return sortBy; }
    public void    setSortBy(String s)      { this.sortBy = s; }

    public String  getSortDir()             { return sortDir; }
    public void    setSortDir(String s)     { this.sortDir = s; }
}
