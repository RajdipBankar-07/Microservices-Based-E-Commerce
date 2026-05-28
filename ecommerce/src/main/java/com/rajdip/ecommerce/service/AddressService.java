package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.dto.AddressRequest;
import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.model.Address;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.AddressRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private static final int MAX_ADDRESSES_PER_USER = 10;

    private final AddressRepository       addressRepository;
    private final UserRepository          userRepository;
    private final MongoTemplate           mongoTemplate;
    private final SequenceGeneratorService sequenceService;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository,
                          MongoTemplate mongoTemplate,
                          SequenceGeneratorService sequenceService) {
        this.addressRepository = addressRepository;
        this.userRepository    = userRepository;
        this.mongoTemplate     = mongoTemplate;
        this.sequenceService   = sequenceService;
    }

    // ── 1. Add Address ─────────────────────────────────────────────────────────

    public ApiResponse<Address> addAddress(Long userId, AddressRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return new ApiResponse<>("User not found", null);

        long count = addressRepository.countByUser_Id(userId);
        if (count >= MAX_ADDRESSES_PER_USER)
            return new ApiResponse<>("Maximum of " + MAX_ADDRESSES_PER_USER + " addresses allowed", null);

        if (addressRepository.existsByUser_IdAndStreetAndCityAndPincode(
                userId, request.getStreet().trim(), request.getCity().trim(), request.getPincode().trim()))
            return new ApiResponse<>("This address already exists for the user", null);

        Address address = mapFromRequest(request, userOpt.get());
        address.setId(sequenceService.nextId("addresses"));

        if (count == 0) {
            address.setDefault(true);
        } else if (request.isDefault()) {
            clearDefaultForUser(userId);
            address.setDefault(true);
        }

        return new ApiResponse<>("Address added successfully", addressRepository.save(address));
    }

    // ── 2. Get All Addresses ───────────────────────────────────────────────────

    public ApiResponse<List<Address>> getAddresses(Long userId) {
        if (!userRepository.existsById(userId)) return new ApiResponse<>("User not found", null);
        List<Address> list = addressRepository.findByUser_Id(userId);
        return new ApiResponse<>(list.size() + " address(es) found", list);
    }

    // ── 3. Get Single Address ──────────────────────────────────────────────────

    public ApiResponse<Address> getAddressById(Long userId, Long addressId) {
        return addressRepository.findByIdAndUser_Id(addressId, userId)
                .map(a -> new ApiResponse<>("Address found", a))
                .orElseGet(() -> new ApiResponse<>("Address not found or not owned by this user", null));
    }

    // ── 4. Get Default Address ─────────────────────────────────────────────────

    public ApiResponse<Address> getDefaultAddress(Long userId) {
        if (!userRepository.existsById(userId)) return new ApiResponse<>("User not found", null);
        return addressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .map(a -> new ApiResponse<>("Default address retrieved", a))
                .orElseGet(() -> new ApiResponse<>("No default address set", null));
    }

    // ── 5. Get by Label ────────────────────────────────────────────────────────

    public ApiResponse<List<Address>> getByLabel(Long userId, String label) {
        if (!userRepository.existsById(userId)) return new ApiResponse<>("User not found", null);
        String norm = label.trim().toUpperCase();
        List<Address> list = addressRepository.findByUser_IdAndLabel(userId, norm);
        return new ApiResponse<>(list.size() + " \"" + norm + "\" address(es) found", list);
    }

    // ── 6. Update Address ──────────────────────────────────────────────────────

    public ApiResponse<Address> updateAddress(Long userId, Long addressId, AddressRequest request) {
        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) return new ApiResponse<>("Address not found or not owned by this user", null);

        Address address = addrOpt.get();
        boolean duplicateExists = addressRepository
                .existsByUser_IdAndStreetAndCityAndPincode(userId, request.getStreet().trim(), request.getCity().trim(), request.getPincode().trim())
                && !(address.getStreet().equalsIgnoreCase(request.getStreet().trim())
                  && address.getCity().equalsIgnoreCase(request.getCity().trim())
                  && address.getPincode().equals(request.getPincode().trim()));

        if (duplicateExists) return new ApiResponse<>("Another address with these details already exists", null);

        if (request.isDefault() && !address.isDefault()) clearDefaultForUser(userId);

        address.setLabel(request.getLabel().trim().toUpperCase());
        address.setStreet(request.getStreet().trim());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setCountry(request.getCountry().trim());
        address.setDefault(request.isDefault());

        return new ApiResponse<>("Address updated successfully", addressRepository.save(address));
    }

    // ── 7. Set Default ─────────────────────────────────────────────────────────

    public ApiResponse<Address> setDefault(Long userId, Long addressId) {
        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) return new ApiResponse<>("Address not found or not owned by this user", null);

        clearDefaultForUser(userId);
        Address address = addrOpt.get();
        address.setDefault(true);
        return new ApiResponse<>("Default address updated successfully", addressRepository.save(address));
    }

    // ── 8. Delete Address ──────────────────────────────────────────────────────

    public ApiResponse<String> deleteAddress(Long userId, Long addressId) {
        Optional<Address> addrOpt = addressRepository.findByIdAndUser_Id(addressId, userId);
        if (addrOpt.isEmpty()) return new ApiResponse<>("Address not found or not owned by this user", null);

        Address address = addrOpt.get();
        if (address.isDefault() && addressRepository.countByUser_Id(userId) > 1)
            return new ApiResponse<>("Cannot delete the default address while others exist. Set another as default first.", null);

        addressRepository.delete(address);
        return new ApiResponse<>("Address deleted successfully", "DELETED");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** MongoDB bulk update — replaces JPA @Modifying clearDefaultForUser */
    private void clearDefaultForUser(Long userId) {
        Query q = new Query(Criteria.where("user.$id").is(userId));
        Update u = new Update().set("isDefault", false);
        mongoTemplate.updateMulti(q, u, Address.class);
    }

    private Address mapFromRequest(AddressRequest request, User user) {
        Address address = new Address();
        address.setUser(user);
        address.setLabel((request.getLabel() == null || request.getLabel().isBlank()) ? "HOME" : request.getLabel().trim().toUpperCase());
        address.setStreet(request.getStreet().trim());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setCountry((request.getCountry() == null || request.getCountry().isBlank()) ? "India" : request.getCountry().trim());
        address.setDefault(request.isDefault());
        return address;
    }
}
