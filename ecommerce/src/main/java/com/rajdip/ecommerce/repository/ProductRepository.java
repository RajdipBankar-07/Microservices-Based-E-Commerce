package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // All products under a specific category
    List<Product> findByCategory_Id(Long categoryId);

    // Products with no category assigned
    List<Product> findByCategoryIsNull();
}