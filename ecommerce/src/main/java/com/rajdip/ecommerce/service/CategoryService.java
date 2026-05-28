package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CategoryRequest;
import com.rajdip.ecommerce.model.Category;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.repository.CategoryRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final SequenceGeneratorService sequenceService;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           SequenceGeneratorService sequenceService) {
        this.categoryRepository = categoryRepository;
        this.productRepository  = productRepository;
        this.sequenceService    = sequenceService;
    }

    // ── 1. Create Category ─────────────────────────────────────────────────────

    public ApiResponse<Category> create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            return new ApiResponse<>("Category with name '" + request.getName() + "' already exists", null);
        }

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setId(sequenceService.nextId("categories"));
        category.setCreatedAt(LocalDateTime.now());

        return new ApiResponse<>("Category created successfully", categoryRepository.save(category));
    }

    // ── 2. Get All Categories ──────────────────────────────────────────────────

    public ApiResponse<List<Category>> getAll() {
        return new ApiResponse<>("Categories retrieved", categoryRepository.findAll());
    }

    // ── 3. Get Category by ID ──────────────────────────────────────────────────

    public ApiResponse<Category> getById(Long id) {
        return categoryRepository.findById(id)
                .map(c -> new ApiResponse<>("Category found", c))
                .orElse(new ApiResponse<>("Category not found", null));
    }

    // ── 4. Update Category ─────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<Category> update(Long id, CategoryRequest request) {
        Optional<Category> existing = categoryRepository.findById(id);
        if (existing.isEmpty()) {
            return new ApiResponse<>("Category not found", null);
        }

        // Ensure the new name doesn't collide with another category
        Optional<Category> duplicate = categoryRepository.findByNameIgnoreCase(request.getName());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            return new ApiResponse<>("Category with name '" + request.getName() + "' already exists", null);
        }

        Category category = existing.get();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setUpdatedAt(LocalDateTime.now());

        return new ApiResponse<>("Category updated successfully", categoryRepository.save(category));
    }

    // ── 5. Delete Category ─────────────────────────────────────────────────────

    /**
     * Deletes a category.
     * All products under this category have their category set to null (unassigned).
     */
    @Transactional
    public ApiResponse<String> delete(Long id) {
        Optional<Category> existing = categoryRepository.findById(id);
        if (existing.isEmpty()) {
            return new ApiResponse<>("Category not found", null);
        }

        // Unlink all products from this category before deleting
        List<Product> products = productRepository.findByCategory_Id(id);
        for (Product product : products) {
            product.setCategory(null);
            productRepository.save(product);
        }

        categoryRepository.deleteById(id);
        return new ApiResponse<>(
            "Category deleted. " + products.size() + " product(s) moved to Uncategorized.",
            "success"
        );
    }

    // ── 6. Assign Category to Product ─────────────────────────────────────────

    /**
     * Assigns or changes the category of a specific product.
     */
    @Transactional
    public ApiResponse<Product> assignCategory(Long productId, Long categoryId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return new ApiResponse<>("Category not found", null);
        }

        Product product = productOpt.get();
        product.setCategory(categoryOpt.get());

        return new ApiResponse<>("Category assigned to product successfully", productRepository.save(product));
    }

    // ── 7. Remove Category from Product ───────────────────────────────────────

    @Transactional
    public ApiResponse<Product> removeCategory(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        Product product = productOpt.get();
        product.setCategory(null);

        return new ApiResponse<>("Category removed from product", productRepository.save(product));
    }

    // ── 8. Get Products by Category ────────────────────────────────────────────

    public ApiResponse<List<Product>> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            return new ApiResponse<>("Category not found", null);
        }

        List<Product> products = productRepository.findByCategory_Id(categoryId);
        return new ApiResponse<>(
            products.size() + " product(s) found in category",
            products
        );
    }

    // ── 9. Get Uncategorized Products ──────────────────────────────────────────

    public ApiResponse<List<Product>> getUncategorized() {
        List<Product> products = productRepository.findByCategoryIsNull();
        return new ApiResponse<>(products.size() + " uncategorized product(s) found", products);
    }
}
