package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class HelloController {

    private final UserService userService;

    public HelloController(UserService userService) {
        this.userService = userService;
    }

    // CREATE
    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody User user) {
        User savedUser = userService.createUser(user);
        return new ApiResponse<>("User created successfully", savedUser);
    }

    // READ ALL
    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        return new ApiResponse<>("Users fetched", userService.getAllUsers());
    }

    // READ BY ID
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        return new ApiResponse<>(userService.deleteUser(id), null);
    }
}