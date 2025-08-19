package com.university.booking.service;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.entity.Building;
import com.university.booking.mapper.BuildingMapper;
import com.university.booking.repository.BuildingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepo;

    public BuildingService(BuildingRepository buildingRepo) {
        this.buildingRepo = buildingRepo;
    }

    public BuildingDTO createBuilding(BuildingDTO dto) {
        Building building = BuildingMapper.toEntity(dto);
        return BuildingMapper.toDTO(buildingRepo.save(building));
    }

    public BuildingDTO activateBuilding(Long id) {
        Building building = getBuildingEntityById(id);
        building.setActive(true);
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
        return BuildingMapper.toDTO(buildingRepo.save(existing));
    }

    public void deleteBuilding(Long id) {
        buildingRepo.deleteById(id);
    }

    private Building getBuildingEntityById(Long id) {
        return buildingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Building not found"));
    }
}
