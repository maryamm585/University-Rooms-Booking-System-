package com.university.booking.controller;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.service.BuildingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createbuilding")
    public ResponseEntity<BuildingDTO> createBuilding(@RequestBody BuildingDTO buildingDTO) {
        return ResponseEntity.ok(buildingService.createBuilding(buildingDTO));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/buildingbyid/{id}")
    public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/allbuildings")
    public ResponseEntity<List<BuildingDTO>> getAllBuildings() {
        return ResponseEntity.ok(buildingService.getAllBuildings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/activebuildings")
    public ResponseEntity<List<BuildingDTO>> getAllActiveBuildings() {
        return ResponseEntity.ok(buildingService.getAllActiveBuildings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updatebuilding/{id}")
    public ResponseEntity<BuildingDTO> updateBuilding(
            @PathVariable Long id,
            @RequestBody BuildingDTO buildingDTO
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(id, buildingDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activatebuilding/{id}")
    public ResponseEntity<BuildingDTO> activateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.activateBuilding(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivatebuilding/{id}")
    public ResponseEntity<BuildingDTO> deactivateBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.deactivateBuilding(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deletebuilding/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
