package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends MongoRepository<Address, Long> {

    @Query(value = "{ 'user.$id': ?0 }", sort = "{ 'isDefault': -1, 'id': 1 }")
    List<Address>    findByUser_Id(Long userId);

    long             countByUser_Id(Long userId);
    Optional<Address> findByIdAndUser_Id(Long id, Long userId);
    Optional<Address> findByUser_IdAndIsDefaultTrue(Long userId);
    List<Address>    findByUser_IdAndLabel(Long userId, String label);

    boolean existsByUser_IdAndStreetAndCityAndPincode(
            Long userId, String street, String city, String pincode);
}
