package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.LoginRequest;
import com.rajdip.ecommerce.dto.JwtInfoDTO;
import com.rajdip.ecommerce.dto.UserResponseDTO;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Authentication", description = "Endpoints for user registration, login, token management, and profile access")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with name, email, and password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User savedUser = service.register(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Wrong password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {

        ApiResponse<String> result = service.login(loginRequest.getEmail(), loginRequest.getPassword());

        if ("Login Successful".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        } else if ("Account Deactivated".equals(result.getMessage())) {
            return ResponseEntity.status(403).body(result);
        } else if ("Wrong Password".equals(result.getMessage())) {
            return ResponseEntity.status(401).body(result);
        } else {
            return ResponseEntity.status(404).body(result);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve authenticated user's profile information")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        return service.getCurrentUser(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validate JWT token", description = "Check if a JWT token is valid and return its claims (email, role, expiration)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<ApiResponse<JwtInfoDTO>> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        ApiResponse<JwtInfoDTO> result = service.validateToken(authorization);

        if ("Token is valid".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(401).body(result);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Generate a new JWT token for authenticated user")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New token generated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> refreshToken(Authentication authentication) {
        ApiResponse<String> result = service.refreshToken(authentication.getName());

        if ("Token refreshed successfully".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(404).body(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate current JWT token by adding it to blacklist")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token")
    })
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        ApiResponse<String> result = service.logout(authorization);

        if ("Logout successful".equals(result.getMessage())) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(401).body(result);
    }
}

