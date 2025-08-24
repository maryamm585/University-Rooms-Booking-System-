package com.university.booking.controller;

import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.RoomFeature;
import com.university.booking.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/v1/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // CREATE A ROOM
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<RoomDTO> createRoom(
            @RequestBody RoomDTO roomDTO,
            @RequestParam Long buildingId
    ) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO, buildingId));
    }


    // GET ROOM BY ID
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // GET ALL ROOMS
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // GET ACTIVE ROOMS
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/active")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }

    // GET ROOMS IN A BUILDING
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(roomService.getRoomsByBuilding(buildingId));
    }

    // UPDATE ROOM WITH ID
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDTO roomDTO
    ) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    // ACTIVATE ROOM BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<RoomDTO> activateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.activateRoom(id));
    }

    // DEACTIVATE ROOM BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<RoomDTO> deactivateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.deactivateRoom(id));
    }

    // GET ROOM FEATURES BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/features/{id}")
    public ResponseEntity<?> getRoomFeatures(@PathVariable Long id) {
        try {
            Set<RoomFeature> features = roomService.getFeaturesByRoomId(id);
            return ResponseEntity.ok(features);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // DELETE ROOM BY ID
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
