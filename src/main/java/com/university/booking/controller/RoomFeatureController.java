package com.university.booking.controller;

import com.university.booking.entity.RoomFeature;
import com.university.booking.service.RoomFeatureService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/roomfeature")
public class RoomFeatureController {
    private final RoomFeatureService featureService;

    public RoomFeatureController(RoomFeatureService featureService) {
        this.featureService = featureService;
    }

    // CREATE NEW FEATURE
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> createFeature(@RequestBody RoomFeature feature) {
        try {
            RoomFeature createdFeature = featureService.createFeature(feature);
            return new ResponseEntity<>(createdFeature, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // GET FEATURE BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getFeatureById(@PathVariable Long id) {
        try {
            RoomFeature feature = featureService.getFeatureById(id);
            return ResponseEntity.ok(feature);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GET ALL FEATURES
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<RoomFeature>> getAllFeatures() {
        List<RoomFeature> features = featureService.getAllFeatures();
        return ResponseEntity.ok(features);
    }

    // UPDATE FEATURE
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeature(@PathVariable Long id, @RequestBody RoomFeature feature) {
        try {
            RoomFeature updatedFeature = featureService.updateFeature(id, feature);
            return ResponseEntity.ok(updatedFeature);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // DELETE FEATURE
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeature(@PathVariable Long id) {
        try {
            featureService.deleteFeature(id);
            return new ResponseEntity<>("Feature deleted successfully", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
