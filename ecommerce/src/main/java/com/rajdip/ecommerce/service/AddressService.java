package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.AddressRequest;
import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.model.Address;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.AddressRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Day 9 — User Address Service.
 *
 * Business rules enforced here:
 *  - Max 10 addresses per user
 *  - Only ONE default address per user (auto-unset previous default)
 *  - First address added is always set as default automatically
 *  - Duplicate address detection (same street+city+pincode per user)
 *  - Ownership check: users can only modify their own addresses
 *  - Cannot delete the default address if other addresses exist (must re-assign first)
 */
@Service
public class AddressService {

    private static final int MAX_ADDRESSES_PER_USER = 10;

    private final AddressRepository addressRepository;
    private final UserRepository    userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository    = userRepository;
    }

    // ── 1. Add Address ─────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<Address> addAddress(Long userId, AddressRequest request) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found", null);
        }

        // Cap: max 10 addresses per user
        long count = addressRepository.countByUser_Id(userId);
        if (count >= MAX_ADDRESSES_PER_USER) {
            return new ApiResponse<>(
                    "Maximum of " + MAX_ADDRESSES_PER_USER + " addresses allowed per user", null);
        }

        // Duplicate check
        if (addressRepository.existsByUser_IdAndStreetAndCityAndPincode(
                userId,
                request.getStreet().trim(),
                request.getCity().trim(),
                request.getPincode().trim())) {
            return new ApiResponse<>("This address already exists for the user", null);
        }

        Address address = mapFromRequest(request, userOpt.get());

        // First address → automatically set as default
        if (count == 0) {
            address.setDefault(true);
        } else if (request.isDefault()) {
            // User explicitly wants this as default → unset all others
            addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        }

        Address saved = addressRepository.save(address);
        return new ApiResponse<>("Address added successfully", saved);
    }

    // ── 2. Get All Addresses for User ──────────────────────────────────────────

    public ApiResponse<List<Address>> getAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        List<Address> list = addressRepository.findByUser_Id(userId);
        return new ApiResponse<>(list.size() + " address(es) found", list);
    }

    // ── 3. Get Single Address (ownership enforced) ─────────────────────────────

    public ApiResponse<Address> getAddressById(Long userId, Long addressId) {
        Optional<Address> address = addressRepository.findByIdAndUser_Id(addressId, userId);
        return address.map(a -> new ApiResponse<>("Address found", a))
                .orElseGet(() -> new ApiResponse<>("Address not found or not owned by this user", null));
    }

    // ── 4. Get Default Address ─────────────────────────────────────────────────

    public ApiResponse<Address> getDefaultAddress(Long userId) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        Optional<Address> addr = addressRepository.findByUser_IdAndIsDefaultTrue(userId);
        return addr.map(a -> new ApiResponse<>("Default address retrieved", a))
                .orElseGet(() -> new ApiResponse<>("No default address set", null));
    }

    // ── 5. Get Addresses by Label ──────────────────────────────────────────────

    public ApiResponse<List<Address>> getByLabel(Long userId, String label) {
        if (!userRepository.existsById(userId)) {
            return new ApiResponse<>("User not found", null);
        }
        String normalizedLabel = label.trim().toUpperCase();
        List<Address> list = addressRepository.findByUser_IdAndLabel(userId, normalizedLabel);
        return new ApiResponse<>(list.size() + " \"" + normalizedLabel + "\" address(es) found", list);
    }

    // ── 6. Update Address ──────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<Address> updateAddress(Long userId, Long addressId, AddressRequest request) {

        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) {
            return new ApiResponse<>("Address not found or not owned by this user", null);
        }

        Address address = addrOpt.get();

        // Duplicate check (exclude self)
        boolean duplicateExists = addressRepository
                .existsByUser_IdAndStreetAndCityAndPincode(
                        userId,
                        request.getStreet().trim(),
                        request.getCity().trim(),
                        request.getPincode().trim())
                && !(address.getStreet().equalsIgnoreCase(request.getStreet().trim())
                  && address.getCity().equalsIgnoreCase(request.getCity().trim())
                  && address.getPincode().equals(request.getPincode().trim()));

        if (duplicateExists) {
            return new ApiResponse<>("Another address with these details already exists", null);
        }

        // If marking this as default → unset others first
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.clearDefaultForUser(userId);
        }

        // Apply updates
        address.setLabel(request.getLabel().trim().toUpperCase());
        address.setStreet(request.getStreet().trim());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setCountry(request.getCountry().trim());
        address.setDefault(request.isDefault());

        Address saved = addressRepository.save(address);
        return new ApiResponse<>("Address updated successfully", saved);
    }

    // ── 7. Set Default Address ─────────────────────────────────────────────────

    @Transactional
    public ApiResponse<Address> setDefault(Long userId, Long addressId) {
        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) {
            return new ApiResponse<>("Address not found or not owned by this user", null);
        }

        // Unset all defaults for this user
        addressRepository.clearDefaultForUser(userId);

        // Set this one
        Address address = addrOpt.get();
        address.setDefault(true);
        Address saved = addressRepository.save(address);
        return new ApiResponse<>("Default address updated successfully", saved);
    }

    // ── 8. Delete Address ──────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> deleteAddress(Long userId, Long addressId) {

        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) {
            return new ApiResponse<>("Address not found or not owned by this user", null);
        }

        Address address = addrOpt.get();

        // Guard: cannot delete default if other addresses exist
        if (address.isDefault() && addressRepository.countByUser_Id(userId) > 1) {
            return new ApiResponse<>(
                    "Cannot delete the default address while other addresses exist. " +
                    "Please set another address as default first.", null);
        }

        addressRepository.delete(address);
        return new ApiResponse<>("Address deleted successfully", "DELETED");
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private Address mapFromRequest(AddressRequest request, User user) {
        Address address = new Address();
        address.setUser(user);
        address.setLabel(
                (request.getLabel() == null || request.getLabel().isBlank())
                        ? "HOME"
                        : request.getLabel().trim().toUpperCase()
        );
        address.setStreet(request.getStreet().trim());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setCountry(
                (request.getCountry() == null || request.getCountry().isBlank())
                        ? "India"
                        : request.getCountry().trim()
        );
        address.setDefault(request.isDefault());
        return address;
    }
}
