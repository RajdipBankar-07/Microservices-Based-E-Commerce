package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.WishlistResponseDTO;
import com.rajdip.ecommerce.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@Tag(name = "Wishlist Management", description = "Endpoints for logged-in users to save and manage favorite products")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    @Operation(summary = "Get user's wishlist", description = "Retrieve all saved products in authenticated user's wishlist")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> getWishlist(Authentication authentication) {
        ApiResponse<WishlistResponseDTO> response = wishlistService.getWishlistByUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/{productId}")
    @Operation(summary = "Add item to wishlist", description = "Save a specific product by ID to user's wishlist")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> addToWishlist(Authentication authentication, @PathVariable Long productId) {
        ApiResponse<WishlistResponseDTO> response = wishlistService.addProductToWishlist(authentication.getName(), productId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Remove item from wishlist", description = "Remove a specific product by ID from user's wishlist")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> removeFromWishlist(Authentication authentication, @PathVariable Long productId) {
        ApiResponse<WishlistResponseDTO> response = wishlistService.removeProductFromWishlist(authentication.getName(), productId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire wishlist", description = "Delete all saved products in user's wishlist")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<String>> clearWishlist(Authentication authentication) {
        ApiResponse<String> response = wishlistService.clearWishlist(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
