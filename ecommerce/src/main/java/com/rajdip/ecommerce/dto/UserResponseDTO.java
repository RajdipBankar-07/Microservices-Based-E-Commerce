package com.rajdip.ecommerce.dto;

// We NEVER send the full User entity back (it contains password).
// This DTO is a safe, read-only view of the user sent in responses.
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;

    // Constructor — called in UserService to build the safe response
    public UserResponseDTO(Long id, String name, String email, String role) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.role  = role;
    }

    // Getters only — no setters needed on a response object
    public Long getId()     { return id; }
    public String getName() { return name; }
    public String getEmail(){ return email; }
    public String getRole() { return role; }
}
