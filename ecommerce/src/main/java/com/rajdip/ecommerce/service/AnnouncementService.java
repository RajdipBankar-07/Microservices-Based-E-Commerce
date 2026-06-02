package com.rajdip.ecommerce.service;

import com.rajdip.ecommerce.model.Announcement;
import com.rajdip.ecommerce.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repo;
    private final SequenceGeneratorService sequenceService;

    public AnnouncementService(AnnouncementRepository repo, SequenceGeneratorService sequenceService) {
        this.repo = repo;
        this.sequenceService = sequenceService;
    }

    public List<Announcement> getAll() {
        return repo.findAll();
    }

    public List<Announcement> getActive() {
        LocalDateTime now = LocalDateTime.now();
        return repo.findByActiveTrue().stream()
                .filter(a -> a.getDisplayUntil() == null || a.getDisplayUntil().isAfter(now))
                .toList();
    }

    public Announcement save(Announcement announcement) {
        if (announcement.getId() == null) {
            announcement.setId(sequenceService.nextId("announcements"));
        }
        return repo.save(announcement);
    }

    public Optional<Announcement> update(Long id, Announcement updated) {
        return repo.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setMessage(updated.getMessage());
            existing.setProduct(updated.getProduct());
            existing.setDisplayUntil(updated.getDisplayUntil());
            existing.setActive(updated.isActive());
            return repo.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
