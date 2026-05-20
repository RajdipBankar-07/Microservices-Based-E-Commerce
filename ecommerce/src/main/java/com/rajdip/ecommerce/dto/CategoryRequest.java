package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a Category.
 */
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getDescription()             { return description; }
    public void setDescription(String desc)    { this.description = desc; }
}
