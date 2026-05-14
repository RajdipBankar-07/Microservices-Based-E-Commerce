package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.CartRequest;
import com.rajdip.ecommerce.dto.CartSummaryDTO;
import com.rajdip.ecommerce.model.CartItem;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.CartRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ── Add / Update item ──────────────────────────────────────────────────────

    /**
     * Add a product to the cart.
     * If the product is already in the cart, its quantity is INCREASED by the requested amount.
     */
    @Transactional
    public ApiResponse<CartItem> addToCart(CartRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        Product product = productOpt.get();

        // check if the item is already in the cart
        Optional<CartItem> existingItem =
                cartRepository.findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // increase quantity
            cartItem = existingItem.get();
            int newQty = cartItem.getQuantity() + request.getQuantity();
            if (newQty > product.getQuantity()) {
                return new ApiResponse<>("Insufficient stock. Available: " + product.getQuantity(), null);
            }
            cartItem.setQuantity(newQty);
        } else {
            // new cart item
            if (request.getQuantity() > product.getQuantity()) {
                return new ApiResponse<>("Insufficient stock. Available: " + product.getQuantity(), null);
            }
            cartItem = new CartItem();
            cartItem.setUser(userOpt.get());
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        CartItem saved = cartRepository.save(cartItem);
        return new ApiResponse<>("Item added to cart", saved);
    }

    // ── View cart ──────────────────────────────────────────────────────────────

    /**
     * Returns all items in a user's cart plus the total price.
     */
    public ApiResponse<CartSummaryDTO> getCart(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }

        List<CartItem> items = cartRepository.findByUser_Id(userId);

        double totalPrice = items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        return new ApiResponse<>("Cart retrieved successfully", new CartSummaryDTO(items, totalPrice));
    }

    // ── Update quantity ────────────────────────────────────────────────────────

    /**
     * Set the quantity of a specific cart item to the exact value provided.
     */
    @Transactional
    public ApiResponse<CartItem> updateQuantity(Long cartItemId, int newQuantity) {
        if (newQuantity < 1) {
            return new ApiResponse<>("Quantity must be at least 1", null);
        }

        Optional<CartItem> itemOpt = cartRepository.findById(cartItemId);
        if (itemOpt.isEmpty()) {
            return new ApiResponse<>("Cart item not found", null);
        }

        CartItem item = itemOpt.get();
        if (newQuantity > item.getProduct().getQuantity()) {
            return new ApiResponse<>("Insufficient stock. Available: " + item.getProduct().getQuantity(), null);
        }

        item.setQuantity(newQuantity);
        return new ApiResponse<>("Cart item updated", cartRepository.save(item));
    }

    // ── Remove one item ────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> removeItem(Long cartItemId) {
        if (!cartRepository.existsById(cartItemId)) {
            return new ApiResponse<>("Cart item not found", null);
        }
        cartRepository.deleteById(cartItemId);
        return new ApiResponse<>("Item removed from cart", "success");
    }

    // ── Clear entire cart ──────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> clearCart(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        cartRepository.deleteByUser_Id(userId);
        return new ApiResponse<>("Cart cleared", "success");
    }

    // ── Checkout: convert cart → orders ───────────────────────────────────────

    /**
     * Checkout converts every cart item into a real Order (delegates to OrderService)
     * and then clears the cart.
     */
    @Transactional
    public ApiResponse<String> checkout(Long userId, OrderService orderService) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }

        List<CartItem> items = cartRepository.findByUser_Id(userId);
        if (items.isEmpty()) {
            return new ApiResponse<>("Cart is empty", null);
        }

        int placedCount = 0;
        StringBuilder errors = new StringBuilder();

        for (CartItem item : items) {
            com.rajdip.ecommerce.dto.OrderRequest req = new com.rajdip.ecommerce.dto.OrderRequest();
            req.setUserId(userId);
            req.setProductId(item.getProduct().getId());
            req.setQuantity(item.getQuantity());

            com.rajdip.ecommerce.dto.ApiResponse<com.rajdip.ecommerce.model.Order> result =
                    orderService.placeOrder(req);

            if (result.getData() != null) {
                placedCount++;
            } else {
                errors.append(item.getProduct().getName())
                      .append(": ")
                      .append(result.getMessage())
                      .append("; ");
            }
        }

        // clear cart regardless (even partial checkout)
        cartRepository.deleteByUser_Id(userId);

        if (errors.length() > 0) {
            return new ApiResponse<>(
                    placedCount + " order(s) placed. Errors: " + errors,
                    "partial");
        }
        return new ApiResponse<>("Checkout successful! " + placedCount + " order(s) placed.", "success");
    }
}
