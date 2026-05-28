package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.dto.JwtInfoDTO;
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

    @Autowired
    private SequenceGeneratorService sequenceService;

    @Autowired
    private EmailService emailService;

    // Register User
    public User register(User user) {
        user.setId(sequenceService.nextId("users"));
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(normalizeRole(user.getRole()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = repo.save(user);
        saved.setPassword(null);
        // Day 6: send welcome email asynchronously
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getName());
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

    public ApiResponse<JwtInfoDTO> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return new ApiResponse<>("Token is missing", null);
        }

        String rawToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        if (!jwtService.isTokenValid(rawToken)) {
            return new ApiResponse<>("Token is invalid or expired", null);
        }

        JwtInfoDTO info = new JwtInfoDTO(
                jwtService.extractUsername(rawToken),
                jwtService.extractRole(rawToken),
                jwtService.extractExpiration(rawToken)
        );

        return new ApiResponse<>("Token is valid", info);
    }

    public ApiResponse<String> refreshToken(String email) {
        Optional<User> user = repo.findByEmail(email);

        if (user.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        String token = jwtService.generateToken(user.get().getEmail(), normalizeRole(user.get().getRole()));
        return new ApiResponse<>("Token refreshed successfully", token);
    }

    public ApiResponse<String> logout(String token) {
        if (token == null || token.isBlank()) {
            return new ApiResponse<>("Token is missing", null);
        }

        String rawToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        if (jwtService.isTokenBlacklisted(rawToken)) {
            return new ApiResponse<>("Token already logged out", null);
        }

        jwtService.blacklistToken(rawToken);
        return new ApiResponse<>("Logout successful", null);
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

