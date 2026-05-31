package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface WishlistRepository extends MongoRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser_Id(Long userId);
    Optional<Wishlist> findByUser_Email(String email);
}
