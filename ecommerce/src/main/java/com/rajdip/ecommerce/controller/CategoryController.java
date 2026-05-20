package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CategoryRequest;
import com.rajdip.ecommerce.model.Category;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(
    name = "Category Management",
    description = "Endpoints for product categories — CRUD (Admin), product assignment, and product listing per category"
)
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ── POST /categories ───────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Create a new category (Admin)",
        description = "Add a new product category. Category names must be unique. (ADMIN only)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Duplicate category name or invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody CategoryRequest request) {
        ApiResponse<Category> response = categoryService.create(request);

        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /categories ────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Retrieve the full list of available product categories."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    // ── GET /categories/{id} ───────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieve a specific category by its ID."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Long id) {
        ApiResponse<Category> response = categoryService.getById(id);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PUT /categories/{id} ───────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a category (Admin)",
        description = "Modify an existing category's name and/or description. (ADMIN only)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Duplicate name or invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<Category>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        ApiResponse<Category> response = categoryService.update(id, request);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Category not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── DELETE /categories/{id} ────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a category (Admin)",
        description = "Remove a category. All products under it are moved to 'Uncategorized'. (ADMIN only)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted, products unlinked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        ApiResponse<String> response = categoryService.delete(id);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /categories/{id}/products ──────────────────────────────────────────

    @GetMapping("/{id}/products")
    @Operation(
        summary = "Get all products in a category",
        description = "Retrieve every product belonging to a specific category."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@PathVariable Long id) {
        ApiResponse<List<Product>> response = categoryService.getProductsByCategory(id);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PATCH /categories/assign ───────────────────────────────────────────────

    @PatchMapping("/assign")
    @Operation(
        summary = "Assign category to a product (Admin)",
        description = "Link a product to a category. If the product already has a category, it is overwritten. (ADMIN only)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category assigned to product"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or Category not found")
    })
    public ResponseEntity<ApiResponse<Product>> assignCategory(
            @RequestParam Long productId,
            @RequestParam Long categoryId) {

        ApiResponse<Product> response = categoryService.assignCategory(productId, categoryId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PATCH /categories/remove-product ──────────────────────────────────────

    @PatchMapping("/remove-product")
    @Operation(
        summary = "Remove category from a product (Admin)",
        description = "Unlink a product from its current category (moves it to Uncategorized). (ADMIN only)"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category removed from product"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<Product>> removeCategory(@RequestParam Long productId) {
        ApiResponse<Product> response = categoryService.removeCategory(productId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /categories/uncategorized ──────────────────────────────────────────

    @GetMapping("/uncategorized")
    @Operation(
        summary = "Get uncategorized products",
        description = "Retrieve all products that have no category assigned."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Uncategorized products retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<Product>>> getUncategorized() {
        return ResponseEntity.ok(categoryService.getUncategorized());
    }
}
