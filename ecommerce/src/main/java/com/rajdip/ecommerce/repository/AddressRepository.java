package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // All addresses for a user (ordered: default first, then by id)
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.id ASC")
    List<Address> findByUser_Id(@Param("userId") Long userId);

    // Count of addresses per user
    long countByUser_Id(Long userId);

    // Get a specific address belonging to a specific user (ownership check)
    Optional<Address> findByIdAndUser_Id(Long id, Long userId);

    // Find the current default address for a user
    Optional<Address> findByUser_IdAndIsDefaultTrue(Long userId);

    // Find addresses by label (HOME/WORK/OTHER)
    List<Address> findByUser_IdAndLabel(Long userId, String label);

    // Unset default flag for ALL addresses of a user (called before setting new default)
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);

    // Check if an exact address already exists for the user
    boolean existsByUser_IdAndStreetAndCityAndPincode(
            Long userId, String street, String city, String pincode);
}
