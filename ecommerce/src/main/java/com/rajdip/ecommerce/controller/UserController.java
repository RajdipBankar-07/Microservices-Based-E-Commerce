package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    // Register API
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = service.register(user);
        return ResponseEntity.ok(savedUser);
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {

        String result = service.login(user.getEmail(), user.getPassword());

        if (result.equals("Login Successful")) {
            return ResponseEntity.ok(result); // 200 OK
        } else if (result.equals("Wrong Password")) {
            return ResponseEntity.status(401).body(result); // 401 Unauthorized
        } else {
            return ResponseEntity.status(404).body(result); // 404 Not Found
        }
    }
}
