package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.OrderRequest;
import com.rajdip.ecommerce.model.Order;
import com.rajdip.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody OrderRequest request) {
        ApiResponse<Order> response = service.placeOrder(request);

        if (response.getData() == null) {
            if ("Insufficient stock".equals(response.getMessage())
                    || "Quantity must be greater than 0".equals(response.getMessage())) {
                return ResponseEntity.status(400).body(response);
            }
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<Order> getAll() {
        return service.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getByUserId(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancel(@PathVariable Long id) {
        ApiResponse<Order> response = service.cancelOrder(id);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Order>> refund(@PathVariable Long id) {
        ApiResponse<Order> response = service.refundOrder(id);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String result = service.deleteOrder(id);
        if ("Order Not Found".equals(result)) {
            return ResponseEntity.status(404).body(result);
        }
        return ResponseEntity.ok(result);
    }
}