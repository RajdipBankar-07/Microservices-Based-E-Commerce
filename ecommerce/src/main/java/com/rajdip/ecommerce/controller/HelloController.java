package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final UserService userService;

    public HelloController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Spring Boot Project Running 🚀";
    }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUserById(id);
    }
}