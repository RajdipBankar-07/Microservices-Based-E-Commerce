package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends MongoRepository<CartItem, Long> {
    List<CartItem>       findByUser_Id(Long userId);
    Optional<CartItem>   findByUser_IdAndProduct_Id(Long userId, Long productId);
    void                 deleteByUser_Id(Long userId);
    boolean              existsByUser_IdAndProduct_Id(Long userId, Long productId);
}
