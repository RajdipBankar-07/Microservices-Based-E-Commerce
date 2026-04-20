package com.rajdip.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // explicitly name the table to avoid conflicts
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // unique = no two users with same email
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "CUSTOMER"; // default role for every new user

    // ── Getters ───────────────────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
