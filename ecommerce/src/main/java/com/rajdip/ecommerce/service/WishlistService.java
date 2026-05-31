package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.WishlistResponseDTO;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.model.Wishlist;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import com.rajdip.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SequenceGeneratorService sequenceService;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           SequenceGeneratorService sequenceService) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.sequenceService = sequenceService;
    }

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setId(sequenceService.nextId("wishlists"));
                    wishlist.setUser(user);
                    wishlist.setProducts(new HashSet<>());
                    return wishlistRepository.save(wishlist);
                });
    }

    public ApiResponse<WishlistResponseDTO> getWishlistByUser(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }
        Wishlist wishlist = getOrCreateWishlist(userOpt.get());
        WishlistResponseDTO dto = new WishlistResponseDTO(wishlist.getId(), email, wishlist.getProducts());
        return new ApiResponse<>("Wishlist retrieved successfully", dto);
    }

    public ApiResponse<WishlistResponseDTO> addProductToWishlist(String email, Long productId) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        Wishlist wishlist = getOrCreateWishlist(userOpt.get());
        wishlist.getProducts().add(productOpt.get());
        wishlistRepository.save(wishlist);

        WishlistResponseDTO dto = new WishlistResponseDTO(wishlist.getId(), email, wishlist.getProducts());
        return new ApiResponse<>("Product added to wishlist", dto);
    }

    public ApiResponse<WishlistResponseDTO> removeProductFromWishlist(String email, Long productId) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        Wishlist wishlist = getOrCreateWishlist(userOpt.get());
        boolean removed = wishlist.getProducts().removeIf(product -> product.getId().equals(productId));
        
        if (!removed) {
            return new ApiResponse<>("Product was not in wishlist", null);
        }
        
        wishlistRepository.save(wishlist);
        WishlistResponseDTO dto = new WishlistResponseDTO(wishlist.getId(), email, wishlist.getProducts());
        return new ApiResponse<>("Product removed from wishlist", dto);
    }

    public ApiResponse<String> clearWishlist(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        Wishlist wishlist = getOrCreateWishlist(userOpt.get());
        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);
        return new ApiResponse<>("Wishlist cleared successfully", "success");
    }
}
