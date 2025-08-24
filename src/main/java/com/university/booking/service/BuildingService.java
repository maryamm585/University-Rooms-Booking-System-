package com.university.booking.service;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.entity.Building;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.mapper.BuildingMapper;
import com.university.booking.repository.BuildingRepository;
import com.university.booking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepo;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(BuildingService.class);

    public BuildingDTO createBuilding(BuildingDTO dto) {
        Building building = BuildingMapper.toEntity(dto);
        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // log with user info
        logger.info("A Building with id {} was created at {} by user with id {} and role {}",
                building.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return BuildingMapper.toDTO(buildingRepo.save(building));
    }

    public BuildingDTO activateBuilding(Long id) {
        Building building = getBuildingEntityById(id);
        building.setActive(true);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Building with id {} was activated at {} by user with id {} and role {}",
                building.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
        return BuildingMapper.toDTO(buildingRepo.save(building));
    }

    public BuildingDTO deactivateBuilding(Long id) {
        Building building = getBuildingEntityById(id);
        building.setActive(false);
        return BuildingMapper.toDTO(buildingRepo.save(building));
    }

    public BuildingDTO getBuildingById(Long id) {
        return BuildingMapper.toDTO(getBuildingEntityById(id));
    }

    public List<BuildingDTO> getAllBuildings() {
        return buildingRepo.findAll()
                .stream()
                .map(BuildingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<BuildingDTO> getAllActiveBuildings() {
        return buildingRepo.findByActiveTrue()
                .stream()
                .map(BuildingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public BuildingDTO updateBuilding(Long id, BuildingDTO dto) {
        Building existing = getBuildingEntityById(id);
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setAddress(dto.getAddress());
        existing.setDescription(dto.getDescription());
        existing.setActive(dto.getActive());

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Building with id {} was updated at {} by user with id {} and role {}",
                existing.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return BuildingMapper.toDTO(buildingRepo.save(existing));
    }

    public void deleteBuilding(Long id) {
        buildingRepo.deleteById(id);
        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Building with id {} was deleted at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

    }

    private Building getBuildingEntityById(Long id) {
        return buildingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Building not found"));
    }
}
