package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
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
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // READ ALL
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
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
    public String deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}