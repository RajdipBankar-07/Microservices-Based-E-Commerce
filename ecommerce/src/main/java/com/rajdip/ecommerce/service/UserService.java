package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.UserResponseDTO;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.UserRepository;
import com.rajdip.ecommerce.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Register User
    public User register(User user) {
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(normalizeRole(user.getRole()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = repo.save(user);
        saved.setPassword(null);
        return saved;
    }

    // Login Logic
    public ApiResponse<String> login(String email, String password) {
        Optional<User> user = repo.findByEmail(email);

        if (user.isPresent()) {
            boolean passwordMatches = passwordEncoder.matches(password, user.get().getPassword());

            if (!passwordMatches && password.equals(user.get().getPassword())) {
                user.get().setPassword(passwordEncoder.encode(password));
                repo.save(user.get());
                passwordMatches = true;
            }

            if (passwordMatches) {
                String token = jwtService.generateToken(user.get().getEmail(), normalizeRole(user.get().getRole()));
                return new ApiResponse<>("Login Successful", token);
            } else {
                return new ApiResponse<>("Wrong Password", null);
            }
        } else {
            return new ApiResponse<>("User Not Found", null);
        }
    }

    public Optional<UserResponseDTO> getCurrentUser(String email) {
        return repo.findByEmail(email)
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "CUSTOMER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }
}

