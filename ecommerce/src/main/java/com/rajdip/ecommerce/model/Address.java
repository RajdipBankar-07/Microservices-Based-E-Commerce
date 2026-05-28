package com.rajdip.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "addresses")
@CompoundIndex(name = "uq_address_user_street_city_pin",
               def = "{'user.$id': 1, 'street': 1, 'city': 1, 'pincode': 1}",
               unique = true)
public class Address {

    @Id
    private Long id;

    @DBRef
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    private String label = "HOME";

    @NotBlank(message = "Street is required")
    @Size(max = 255)
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

    private boolean isDefault = false;

    public Long    getId()      { return id; }
    public User    getUser()    { return user; }
    public String  getLabel()   { return label; }
    public String  getStreet()  { return street; }
    public String  getCity()    { return city; }
    public String  getState()   { return state; }
    public String  getPincode() { return pincode; }
    public String  getCountry() { return country; }
    public boolean isDefault()  { return isDefault; }

    public void setId(Long id)        { this.id = id; }
    public void setUser(User u)       { this.user = u; }
    public void setLabel(String l)    { this.label = l; }
    public void setStreet(String s)   { this.street = s; }
    public void setCity(String c)     { this.city = c; }
    public void setState(String s)    { this.state = s; }
    public void setPincode(String p)  { this.pincode = p; }
    public void setCountry(String c)  { this.country = c; }
    public void setDefault(boolean d) { this.isDefault = d; }
}
