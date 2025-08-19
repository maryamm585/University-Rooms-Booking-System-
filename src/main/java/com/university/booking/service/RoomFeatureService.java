package com.university.booking.service;


import com.university.booking.entity.RoomFeature;
import com.university.booking.repository.RoomFeatureRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RoomFeatureService {

    private final RoomFeatureRepository featureRepo;

    public RoomFeatureService(RoomFeatureRepository featureRepo) {
        this.featureRepo = featureRepo;
    }

    public RoomFeature createFeature(RoomFeature feature) {
        if (featureRepo.existsByName(feature.getName())) {
            throw new IllegalArgumentException("Feature with this name already exists!");
        }
        return featureRepo.save(feature);
    }

    public RoomFeature getFeatureById(Long id) {
        return featureRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found with ID: " + id));
    }

    public List<RoomFeature> getAllFeatures() {
        return featureRepo.findAll();
    }

    public RoomFeature updateFeature(Long id, RoomFeature feature) {
        RoomFeature existing = getFeatureById(id);
        existing.setName(feature.getName());
        return featureRepo.save(existing);
    }

    public void deleteFeature(Long id) {
        if (!featureRepo.existsById(id)) {
            throw new EntityNotFoundException("Feature not found with ID: " + id);
        }
        featureRepo.deleteById(id);
    }
}
