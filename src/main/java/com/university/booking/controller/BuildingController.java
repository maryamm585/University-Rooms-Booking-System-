package com.university.booking.controller;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    // CREATE NEW BUILDING
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<BuildingDTO> createBuilding(@RequestBody BuildingDTO buildingDTO) {
        return ResponseEntity.ok(buildingService.createBuilding(buildingDTO));
    }

    // GET BUILDING BY ID
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    // GET ALL BUILDINGS
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/")
    public ResponseEntity<List<BuildingDTO>> getAllBuildings() {
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    // GET ALL ACTIVE BUILDINGS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<BuildingDTO>> getAllActiveBuildings() {
        return ResponseEntity.ok(buildingService.getAllActiveBuildings());
    }

    // UPDATE An BUILDING
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BuildingDTO> updateBuilding(
            @PathVariable Long id,
            @RequestBody BuildingDTO buildingDTO
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(id, buildingDTO));
    }

    // ACTIVATE A BUILDING
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<BuildingDTO> activateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.activateBuilding(id));
    }

    // DEACTIVATE A BUILDING
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<BuildingDTO> deactivateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.deactivateBuilding(id));
    }

    // DELETE A BUIDLING
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
