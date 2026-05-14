package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    // All items in a user's cart
    List<CartItem> findByUser_Id(Long userId);

    // Specific cart item for a user & product (for upsert logic)
    Optional<CartItem> findByUser_IdAndProduct_Id(Long userId, Long productId);

    // Remove all items of a user (used after checkout)
    void deleteByUser_Id(Long userId);
}
