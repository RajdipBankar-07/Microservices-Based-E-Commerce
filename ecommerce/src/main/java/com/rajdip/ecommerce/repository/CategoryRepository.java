package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Check duplicate name (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Find by name (for lookup)
    Optional<Category> findByNameIgnoreCase(String name);
}
