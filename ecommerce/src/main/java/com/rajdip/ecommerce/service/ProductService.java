package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final SequenceGeneratorService sequenceService;

    public ProductService(ProductRepository repo, SequenceGeneratorService sequenceService) {
        this.repo = repo;
        this.sequenceService = sequenceService;
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(sequenceService.nextId("products"));
        }
        return repo.save(product);
    }

    public List<Product> getAll() {
        return repo.findAll();
    }

    public Optional<Product> getById(Long id) {
        return repo.findById(id);
    }

    public Optional<Product> update(Long id, Product updatedProduct) {
        Optional<Product> existing = repo.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Product product = existing.get();
        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());

        // Preserve or update category if provided
        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }

        return Optional.of(repo.save(product));
    }

    public boolean delete(Long id) {
        if (!repo.existsById(id)) {
            return false;
        }

        repo.deleteById(id);
        return true;
    }

    // ── Category-based queries ─────────────────────────────────────────────────

    public List<Product> getByCategory(Long categoryId) {
        return repo.findByCategory_Id(categoryId);
    }

    public List<Product> getUncategorized() {
        return repo.findByCategoryIsNull();
    }
}