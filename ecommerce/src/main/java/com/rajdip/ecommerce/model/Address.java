package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Day 9 — User Address entity.
 *
 * One user can have many addresses.
 * Exactly ONE address per user can be the default (isDefault = true).
 *
 * Label values: HOME | WORK | OTHER
 */
@Entity
@Table(
    name = "addresses",
    uniqueConstraints = {
        // Prevent exact duplicates: same user, same street, same city, same pincode
        @UniqueConstraint(
            name = "uq_address_user_street_city_pin",
            columnNames = {"user_id", "street", "city", "pincode"}
        )
    }
)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many addresses → one user
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    /**
     * Address label / type.
     * Allowed: HOME | WORK | OTHER (default: HOME)
     */
    @Column(nullable = false, length = 20)
    private String label = "HOME";

    @Column(nullable = false)
    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be at most 255 characters")
    private String street;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "City is required")
    private String city;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "State is required")
    private String state;

    @Column(nullable = false, length = 10)
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{4,10}", message = "Pincode must be 4–10 digits")
    private String pincode;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Country is required")
    private String country = "India";

    /**
     * If true, this is the user's default shipping address.
     * Enforced at service level — only one can be default per user.
     */
    @Column(nullable = false)
    private boolean isDefault = false;

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long    getId()       { return id; }
    public User    getUser()     { return user; }
    public String  getLabel()    { return label; }
    public String  getStreet()   { return street; }
    public String  getCity()     { return city; }
    public String  getState()    { return state; }
    public String  getPincode()  { return pincode; }
    public String  getCountry()  { return country; }
    public boolean isDefault()   { return isDefault; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setId(Long id)           { this.id = id; }
    public void setUser(User user)       { this.user = user; }
    public void setLabel(String label)   { this.label = label; }
    public void setStreet(String s)      { this.street = s; }
    public void setCity(String c)        { this.city = c; }
    public void setState(String s)       { this.state = s; }
    public void setPincode(String p)     { this.pincode = p; }
    public void setCountry(String c)     { this.country = c; }
    public void setDefault(boolean d)    { this.isDefault = d; }
}
