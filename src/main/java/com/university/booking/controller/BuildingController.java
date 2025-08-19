package com.university.booking.controller;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.service.BuildingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PostMapping("/ADMIN/createbuilding")
    public ResponseEntity<BuildingDTO> createBuilding(@RequestBody BuildingDTO buildingDTO) {
        return ResponseEntity.ok(buildingService.createBuilding(buildingDTO));
    }

    @GetMapping("/All/buildingbyid/{id}")
    public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @GetMapping("/ADMIN/allbuildings")
    public ResponseEntity<List<BuildingDTO>> getAllBuildings() {
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    @GetMapping("/All/activebuildings")
    public ResponseEntity<List<BuildingDTO>> getAllActiveBuildings() {
        return ResponseEntity.ok(buildingService.getAllActiveBuildings());
    }

    @PutMapping("/ADMIN/updatebuilding/{id}")
    public ResponseEntity<BuildingDTO> updateBuilding(
            @PathVariable Long id,
            @RequestBody BuildingDTO buildingDTO
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(id, buildingDTO));
    }

    @PutMapping("/ADMIN/activatebuilding/{id}")
    public ResponseEntity<BuildingDTO> activateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.activateBuilding(id));
    }

    @PutMapping("/ADMIN/deactivatebuilding/{id}")
    public ResponseEntity<BuildingDTO> deactivateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.deactivateBuilding(id));
    }

    @DeleteMapping("/ADMIN/deletebuilding/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
