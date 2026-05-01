package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.LoginRequest;
import com.rajdip.ecommerce.dto.JwtInfoDTO;
import com.rajdip.ecommerce.dto.UserResponseDTO;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    // Register API
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User savedUser = service.register(user);
        return ResponseEntity.ok(savedUser);
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {

        ApiResponse<String> result = service.login(loginRequest.getEmail(), loginRequest.getPassword());

        if ("Login Successful".equals(result.getMessage())) {
            return ResponseEntity.ok(result); // 200 OK
        } else if ("Wrong Password".equals(result.getMessage())) {
            return ResponseEntity.status(401).body(result); // 401 Unauthorized
        } else {
            return ResponseEntity.status(404).body(result); // 404 Not Found
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        return service.getCurrentUser(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<JwtInfoDTO>> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        ApiResponse<JwtInfoDTO> result = service.validateToken(authorization);

        if ("Token is valid".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(401).body(result);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshToken(Authentication authentication) {
        ApiResponse<String> result = service.refreshToken(authentication.getName());

        if ("Token refreshed successfully".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(404).body(result);
    }
}

