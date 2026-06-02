package com.rajdip.ecommerce.controller;

import com.rajdip.ecommerce.dto.ApiResponse;
import com.rajdip.ecommerce.model.Announcement;
import com.rajdip.ecommerce.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Announcement & Notifications", description = "Endpoints for user alerts, promotions, and sales banners")
public class AnnouncementController {

    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    // ── Storefront (Customer) Endpoint ─────────────────────────────────────────

    @GetMapping("/announcements")
    @Operation(summary = "Get active announcements", description = "Retrieve list of active sales promotions and scheduled deactivations")
    public ResponseEntity<ApiResponse<List<Announcement>>> getActive() {
        return ResponseEntity.ok(
                new ApiResponse<>("Active announcements retrieved successfully", service.getActive())
        );
    }

    // ── Admin Endpoints ────────────────────────────────────────────────────────

    @GetMapping("/admin/announcements")
    @Operation(summary = "Get all announcements", description = "Retrieve list of all announcements (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<List<Announcement>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>("All announcements retrieved successfully", service.getAll())
        );
    }

    @PostMapping("/admin/announcements")
    @Operation(summary = "Create announcement", description = "Add a new customer notification (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<Announcement>> create(@RequestBody Announcement announcement) {
        Announcement saved = service.save(announcement);
        return ResponseEntity.ok(
                new ApiResponse<>("Announcement created successfully", saved)
        );
    }

    @PutMapping("/admin/announcements/{id}")
    @Operation(summary = "Update announcement", description = "Edit an existing customer notification (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<Announcement>> update(
            @PathVariable Long id, 
            @RequestBody Announcement announcement) {
        return service.update(id, announcement)
                .map(updated -> ResponseEntity.ok(
                        new ApiResponse<>("Announcement updated successfully", updated)
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/announcements/{id}")
    @Operation(summary = "Delete announcement", description = "Permanently remove a customer notification (ADMIN only)")
    @SecurityRequirement(name = "Bearer Auth")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.ok(
                    new ApiResponse<>("Announcement deleted successfully", null)
            );
        }
        return ResponseEntity.notFound().build();
    }
}
