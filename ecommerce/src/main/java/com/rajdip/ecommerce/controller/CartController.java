package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CartRequest;
import com.rajdip.ecommerce.dto.CartSummaryDTO;
import com.rajdip.ecommerce.model.CartItem;
import com.rajdip.ecommerce.service.CartService;
import com.rajdip.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart Management", description = "Endpoints for shopping cart – add, view, update, remove, clear, and checkout")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // ── POST /cart ─────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Add item to cart",
        description = "Add a product to the user's cart. If the product already exists in the cart, its quantity is increased."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added to cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient stock or invalid quantity"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or product not found")
    })
    public ResponseEntity<ApiResponse<CartItem>> addToCart(@Valid @RequestBody CartRequest request) {
        ApiResponse<CartItem> response = cartService.addToCart(request);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if (msg.contains("not found")) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /cart/{userId} ─────────────────────────────────────────────────────

    @GetMapping("/{userId}")
    @Operation(
        summary = "View cart",
        description = "Retrieve all items in a user's cart along with the total price."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<CartSummaryDTO>> getCart(@PathVariable Long userId) {
        ApiResponse<CartSummaryDTO> response = cartService.getCart(userId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PUT /cart/{cartItemId} ─────────────────────────────────────────────────

    @PutMapping("/{cartItemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "Set the exact quantity of a specific cart item."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart item updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient stock or invalid quantity"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<ApiResponse<CartItem>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {

        ApiResponse<CartItem> response = cartService.updateQuantity(cartItemId, quantity);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("Cart item not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── DELETE /cart/item/{cartItemId} ─────────────────────────────────────────

    @DeleteMapping("/item/{cartItemId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Remove a single product from the user's cart."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed from cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<ApiResponse<String>> removeItem(@PathVariable Long cartItemId) {
        ApiResponse<String> response = cartService.removeItem(cartItemId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── DELETE /cart/{userId}/clear ────────────────────────────────────────────

    @DeleteMapping("/{userId}/clear")
    @Operation(
        summary = "Clear entire cart",
        description = "Remove all items from a user's cart."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        ApiResponse<String> response = cartService.clearCart(userId);

        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── POST /cart/{userId}/checkout ───────────────────────────────────────────

    @PostMapping("/{userId}/checkout")
    @Operation(
        summary = "Checkout cart",
        description = "Convert all cart items into real orders and clear the cart. Stock is deducted automatically."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Checkout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cart is empty or stock issues"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> checkout(@PathVariable Long userId) {
        ApiResponse<String> response = cartService.checkout(userId, orderService);

        if (response.getData() == null) {
            String msg = response.getMessage();
            if ("User not found".equals(msg)) return ResponseEntity.status(404).body(response);
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
