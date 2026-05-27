package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.AddressRequest;
import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.model.Address;
import com.rajdip.ecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/addresses")
@Tag(
    name = "User Address Management",
    description = """
        Manage delivery addresses per user:
        - 🏠 Add / update / delete addresses
        - 📌 Set a default delivery address
        - 🏷️ Filter by label (HOME / WORK / OTHER)
        - 🔒 Ownership enforced — users can only access their own addresses

        **Business rules:**
        - Maximum **10 addresses** per user
        - First address is always set as default automatically
        - Only **one** default address per user
        - Cannot delete the default while other addresses exist
        """
)
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // ── POST /users/{userId}/addresses ────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Add a new address",
        description = """
            Adds a new delivery address for the user.

            - **label**: `HOME` | `WORK` | `OTHER` (default: HOME)
            - **isDefault**: set `true` to make it the default — auto-unsets previous default
            - First address added is always set as default automatically
            - Max 10 addresses per user
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error / duplicate / limit reached"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {

        ApiResponse<Address> response = addressService.addAddress(userId, request);
        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /users/{userId}/addresses ─────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all addresses",
        description = "Returns all addresses for the user. Default address appears first."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Addresses retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses(@PathVariable Long userId) {
        ApiResponse<List<Address>> response = addressService.getAddresses(userId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /users/{userId}/addresses/{addressId} ─────────────────────────────

    @GetMapping("/{addressId}")
    @Operation(
        summary = "Get address by ID",
        description = "Retrieves a specific address. Returns 404 if the address doesn't belong to the user."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<ApiResponse<Address>> getById(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        ApiResponse<Address> response = addressService.getAddressById(userId, addressId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /users/{userId}/addresses/default ─────────────────────────────────

    @GetMapping("/default")
    @Operation(
        summary = "Get default address",
        description = "Returns the user's currently active default delivery address."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Default address returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No default address / user not found")
    })
    public ResponseEntity<ApiResponse<Address>> getDefault(@PathVariable Long userId) {
        ApiResponse<Address> response = addressService.getDefaultAddress(userId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /users/{userId}/addresses/label/{label} ───────────────────────────

    @GetMapping("/label/{label}")
    @Operation(
        summary = "Get addresses by label",
        description = "Filter addresses by label. Label is case-insensitive. Valid: `HOME` | `WORK` | `OTHER`"
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Labelled addresses returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<Address>>> getByLabel(
            @PathVariable Long userId,
            @Parameter(description = "Label: HOME | WORK | OTHER") @PathVariable String label) {

        ApiResponse<List<Address>> response = addressService.getByLabel(userId, label);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PUT /users/{userId}/addresses/{addressId} ─────────────────────────────

    @PutMapping("/{addressId}")
    @Operation(
        summary = "Update address",
        description = "Updates an existing address. All fields are updated (full replacement). Set `isDefault: true` to also make it the new default."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        ApiResponse<Address> response = addressService.updateAddress(userId, addressId, request);
        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── PATCH /users/{userId}/addresses/{addressId}/default ───────────────────

    @PatchMapping("/{addressId}/default")
    @Operation(
        summary = "Set as default address",
        description = "Makes the specified address the default delivery address. Automatically clears the previous default."
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Default address updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<ApiResponse<Address>> setDefault(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        ApiResponse<Address> response = addressService.setDefault(userId, addressId);
        if (response.getData() == null) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ── DELETE /users/{userId}/addresses/{addressId} ──────────────────────────

    @DeleteMapping("/{addressId}")
    @Operation(
        summary = "Delete address",
        description = """
            Deletes an address.

            ⚠️ **Cannot delete the default address** while other addresses exist.
            Set another address as default first, then delete this one.
            """
    )
    @SecurityRequirement(name = "Bearer Auth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete default address"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        ApiResponse<String> response = addressService.deleteAddress(userId, addressId);
        if (response.getData() == null) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
