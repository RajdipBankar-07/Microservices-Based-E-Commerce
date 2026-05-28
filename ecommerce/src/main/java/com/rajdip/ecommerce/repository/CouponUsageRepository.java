package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CouponUsageRepository extends MongoRepository<CouponUsage, Long> {

    boolean          existsByCoupon_IdAndUser_Id(Long couponId, Long userId);
    List<CouponUsage> findByCoupon_Id(Long couponId);
    List<CouponUsage> findByUser_Id(Long userId);
}
