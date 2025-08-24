package com.university.booking.service;


import com.university.booking.entity.RoomFeature;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.RoomFeatureRepository;
import com.university.booking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomFeatureService {

    private final RoomFeatureRepository featureRepo;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(RoomFeatureService.class);


    public RoomFeature createFeature(RoomFeature feature) {
        if (featureRepo.existsByName(feature.getName())) {
            throw new IllegalArgumentException("Feature with this name already exists!");
        }

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A New Room Feature with id {} was Created at {} by user with id {} and role {}",
                feature.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

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

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Room Feature with id {} was Updated at {} by user with id {} and role {}",
                feature.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return featureRepo.save(existing);
    }

    public void deleteFeature(Long id) {
        if (!featureRepo.existsById(id)) {
            throw new EntityNotFoundException("Feature not found with ID: " + id);
        }

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Room Feature with id {} was Deleted at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
        featureRepo.deleteById(id);
    }
}
