package com.rajdip.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "categories")
public class Category {

    @Id
    private Long id;

    @NotBlank(message = "Category name is required")
    @Indexed(unique = true)
    private String name;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long          getId()          { return id; }
    public String        getName()        { return name; }
    public String        getDescription() { return description; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setId(Long id)                    { this.id = id; }
    public void setName(String name)              { this.name = name; }
    public void setDescription(String d)          { this.description = d; }
    public void setCreatedAt(LocalDateTime dt)    { this.createdAt = dt; }
    public void setUpdatedAt(LocalDateTime dt)    { this.updatedAt = dt; }
}
