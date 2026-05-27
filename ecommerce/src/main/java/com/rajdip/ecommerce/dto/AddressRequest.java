package com.rajdip.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for create / update address operations.
 *
 * userId is NOT included here — it is taken from the path variable
 * in the controller for security (users can only manage their own addresses).
 */
public class AddressRequest {

    /**
     * Address label: HOME | WORK | OTHER
     * Defaults to HOME if not provided.
     */
    private String label = "HOME";

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be at most 255 characters")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{4,10}", message = "Pincode must be 4–10 digits")
    private String pincode;

    @NotBlank(message = "Country is required")
    private String country = "India";

    /**
     * Set true to make this the default address.
     * Any existing default will be unset automatically.
     */
    private boolean isDefault = false;

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String  getLabel()              { return label; }
    public void    setLabel(String l)      { this.label = l; }

    public String  getStreet()             { return street; }
    public void    setStreet(String s)     { this.street = s; }

    public String  getCity()               { return city; }
    public void    setCity(String c)       { this.city = c; }

    public String  getState()              { return state; }
    public void    setState(String s)      { this.state = s; }

    public String  getPincode()            { return pincode; }
    public void    setPincode(String p)    { this.pincode = p; }

    public String  getCountry()            { return country; }
    public void    setCountry(String c)    { this.country = c; }

    public boolean isDefault()             { return isDefault; }
    public void    setDefault(boolean d)   { this.isDefault = d; }
}
