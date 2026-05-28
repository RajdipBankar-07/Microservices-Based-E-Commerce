package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, Long> {

    // Keyword search — case-insensitive
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // In-stock only
    @Query("{ 'quantity': { $gt: 0 } }")
    List<Product> findInStockProducts();

    // Price range
    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List<Product> findByPriceRange(double minPrice, double maxPrice);

    // Low stock (for dashboard)
    @Query("{ 'quantity': { $lte: ?0, $gt: 0 } }")
    List<Product> findLowStockProducts(int threshold);

    // Out of stock (for dashboard)
    @Query("{ 'quantity': 0 }")
    List<Product> findOutOfStockProducts();

    // By category
    List<Product> findByCategory_Id(Long categoryId);

    List<Product> findByCategoryIsNull();

    // Combined search (keyword + category + price range + in-stock)
    @Query("{ $and: [ " +
           "  { $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { $expr: { $eq: [?0, null] } } ] }, " +
           "  { $or: [ { 'category.$id': ?1 }, { $expr: { $eq: [?1, null] } } ] }, " +
           "  { $or: [ { 'price': { $gte: ?2 } }, { $expr: { $eq: [?2, null] } } ] }, " +
           "  { $or: [ { 'price': { $lte: ?3 } }, { $expr: { $eq: [?3, null] } } ] }, " +
           "  { $or: [ { 'quantity': { $gt: 0 } }, { $expr: { $eq: [?4, false] } } ] } " +
           "] }")
    List<Product> searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, boolean inStockOnly);

    // Paginated (Day 8)
    Page<Product> findAll(Pageable pageable);
}