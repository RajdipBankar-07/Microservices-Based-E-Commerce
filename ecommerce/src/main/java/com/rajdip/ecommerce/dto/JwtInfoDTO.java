package com.rajdip.ecommerce.dto;

import java.util.Date;

public class JwtInfoDTO {

    private String email;
    private String role;
    private Date expiresAt;

    public JwtInfoDTO(String email, String role, Date expiresAt) {
        this.email = email;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}