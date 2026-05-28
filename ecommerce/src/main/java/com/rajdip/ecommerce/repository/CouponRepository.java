package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);
    boolean          existsByCode(String code);
    List<Coupon>     findByIsActiveTrue();

    @Query("{ 'expiryDate': { $lt: ?0 } }")
    List<Coupon> findExpiredCoupons(LocalDate today);

    @Query("{ 'isActive': true, 'expiryDate': { $gte: ?0 }, $or: [ { 'maxUses': 0 }, { $expr: { $lt: ['$currentUses', '$maxUses'] } } ] }")
    List<Coupon> findAllValidCoupons(LocalDate today);
}
