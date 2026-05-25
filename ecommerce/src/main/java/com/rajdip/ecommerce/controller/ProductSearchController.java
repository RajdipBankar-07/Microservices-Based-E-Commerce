package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.ProductSearchRequest;
import com.rajdip.ecommerce.dto.ProductSearchResultDTO;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/search")
@Tag(
    name = "Product Search & Filter",
    description = """
        Powerful product discovery endpoints:
        - 🔍 Keyword search (name contains, case-insensitive)
        - 📂 Filter by category
        - 💰 Filter by price range
        - 📦 Show only in-stock products
        - 🔢 Sort by name / price / id (asc or desc)
        - 🔗 Combine all filters in a single call
        """
)
public class ProductSearchController {

    private final ProductSearchService searchService;

    public ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    // ── GET /products/search ───────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Combined search & filter",
        description = """
            Search products using any combination of optional query parameters:

            | Param        | Type    | Description                               |
            |--------------|---------|-------------------------------------------|
            | keyword      | String  | Name contains (case-insensitive)          |
            | categoryId   | Long    | Filter by category ID                     |
            | minPrice     | Double  | Minimum price (inclusive)                 |
            | maxPrice     | Double  | Maximum price (inclusive)                 |
            | inStockOnly  | boolean | Only show products with quantity > 0      |
            | sortBy       | String  | `name` \\| `price` \\| `id` (default: `id`) |
            | sortDir      | String  | `asc` \\| `desc` (default: `asc`)          |

            **Examples:**
            - `/products/search?keyword=phone`
            - `/products/search?minPrice=100&maxPrice=500&inStockOnly=true`
            - `/products/search?keyword=shirt&categoryId=3&sortBy=price&sortDir=desc`
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ProductSearchResultDTO>> search(
            @Parameter(description = "Keyword to search in product name") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by category ID")              @RequestParam(required = false) Long   categoryId,
            @Parameter(description = "Minimum price (inclusive)")          @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price (inclusive)")          @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Show only in-stock products")        @RequestParam(defaultValue = "false") boolean inStockOnly,
            @Parameter(description = "Sort field: name | price | id")      @RequestParam(defaultValue = "id")    String sortBy,
            @Parameter(description = "Sort direction: asc | desc")         @RequestParam(defaultValue = "asc")   String sortDir) {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setInStockOnly(inStockOnly);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        return ResponseEntity.ok(searchService.search(request));
    }

    // ── GET /products/search/keyword ───────────────────────────────────────────

    @GetMapping("/keyword")
    @Operation(
        summary = "Quick keyword search",
        description = "Fast name-only search. Returns all products whose name contains the keyword (case-insensitive)."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matching products returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Keyword is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Product>>> quickSearch(
            @Parameter(description = "Search keyword", required = true) @RequestParam String keyword) {

        ApiResponse<List<Product>> response = searchService.searchByKeyword(keyword);

        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /products/search/price-range ──────────────────────────────────────

    @GetMapping("/price-range")
    @Operation(
        summary = "Filter by price range",
        description = "Retrieve all products priced between minPrice and maxPrice (inclusive). Results are sorted by price ascending."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products in price range returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid price range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Product>>> filterByPriceRange(
            @Parameter(description = "Minimum price", required = true) @RequestParam double minPrice,
            @Parameter(description = "Maximum price", required = true) @RequestParam double maxPrice) {

        ApiResponse<List<Product>> response = searchService.searchByPriceRange(minPrice, maxPrice);

        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /products/search/in-stock ─────────────────────────────────────────

    @GetMapping("/in-stock")
    @Operation(
        summary = "Get all in-stock products",
        description = "Returns only products with quantity > 0, sorted alphabetically by name."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "In-stock products returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Product>>> getInStock() {
        return ResponseEntity.ok(searchService.getInStockProducts());
    }
}
