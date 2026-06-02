package com.rajdip.ecommerce.repository;

import com.rajdip.ecommerce.model.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnnouncementRepository extends MongoRepository<Announcement, Long> {
    List<Announcement> findByActiveTrue();
}
