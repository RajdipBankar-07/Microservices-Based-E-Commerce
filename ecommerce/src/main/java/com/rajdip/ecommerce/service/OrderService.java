package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.OrderRequest;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.OrderRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ApiResponse<Order> placeOrder(OrderRequest request) {
        if (request.getQuantity() <= 0) {
            return new ApiResponse<>("Quantity must be greater than 0", null);
        }

        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        Optional<Product> productOptional = productRepository.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            return new ApiResponse<>("Product not found", null);
        }

        Product product = productOptional.get();
        if (product.getQuantity() < request.getQuantity()) {
            return new ApiResponse<>("Insufficient stock", null);
        }

        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.save(product);

        Order order = new Order();
        order.setUser(userOptional.get());
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);
        return new ApiResponse<>("Order placed successfully", savedOrder);
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public List<Order> getByUserId(Long userId) {
        return orderRepository.findByUser_Id(userId);
    }

    @Transactional
    public ApiResponse<Order> cancelOrder(Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return new ApiResponse<>("Order not found", null);
        }

        Order order = orderOptional.get();
        if ("CANCELLED".equals(order.getStatus())) {
            return new ApiResponse<>("Order already cancelled", order);
        }
        if ("REFUNDED".equals(order.getStatus())) {
            return new ApiResponse<>("Order already refunded", order);
        }

        restoreStock(order);
        order.setStatus("CANCELLED");
        Order saved = orderRepository.save(order);
        return new ApiResponse<>("Order cancelled and stock restored", saved);
    }

    @Transactional
    public ApiResponse<Order> refundOrder(Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return new ApiResponse<>("Order not found", null);
        }

        Order order = orderOptional.get();
        if ("REFUNDED".equals(order.getStatus())) {
            return new ApiResponse<>("Order already refunded", order);
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return new ApiResponse<>("Cancelled order cannot be refunded", order);
        }

        restoreStock(order);
        order.setStatus("REFUNDED");
        Order saved = orderRepository.save(order);
        return new ApiResponse<>("Order refunded and stock restored", saved);
    }

    @Transactional
    public String deleteOrder(Long id) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return "Order Not Found";
        }

        Order order = orderOptional.get();
        if (!"CANCELLED".equals(order.getStatus()) && !"REFUNDED".equals(order.getStatus())) {
            restoreStock(order);
        }

        orderRepository.deleteById(id);
        return "Order Deleted & Stock Restored";
    }

    private void restoreStock(Order order) {
        Optional<Product> productOptional = productRepository.findById(order.getProduct().getId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setQuantity(product.getQuantity() + order.getQuantity());
            productRepository.save(product);
        }
    }
}