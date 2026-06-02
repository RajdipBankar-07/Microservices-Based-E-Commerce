package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.UserResponseDTO;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin User Management", description = "Endpoints for admin-only user deactivation, reactivation, list viewing, and deletion")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all user accounts on the platform (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(
                new ApiResponse<>("Users retrieved successfully", userService.getAllUsers())
        );
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle user active status", description = "Suspend or reactivate a user account (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or self deactivation attempt"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication authentication) {

        // Prevent admin from deactivating themselves
        String currentAdminEmail = authentication.getName();
        Optional<UserResponseDTO> currentUser = userService.getCurrentUser(currentAdminEmail);

        if (currentUser.isPresent() && currentUser.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Self-deactivation is prohibited. You cannot suspend your own admin account.", null)
            );
        }

        Optional<User> updatedUser = userService.toggleUserStatus(id, active);

        if (updatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String msg = active ? "Account reactivated successfully" : "Account suspended successfully";
        return ResponseEntity.ok(new ApiResponse<>(msg, updatedUser.get()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user account", description = "Permanently remove a user account from the system (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Self deletion attempt"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only")
    })
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        // Prevent admin from deleting themselves
        String currentAdminEmail = authentication.getName();
        Optional<UserResponseDTO> currentUser = userService.getCurrentUser(currentAdminEmail);

        if (currentUser.isPresent() && currentUser.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>("Self-deletion is prohibited. You cannot delete your own admin account.", null)
            );
        }

        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            return ResponseEntity.status(404).body(new ApiResponse<>("User not found", null));
        }

        return ResponseEntity.ok(new ApiResponse<>("User account permanently deleted successfully", null));
    }
}
