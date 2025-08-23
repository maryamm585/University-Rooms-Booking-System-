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
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createroom")
    public ResponseEntity<RoomDTO> createRoom(
            @RequestBody RoomDTO roomDTO,
            @RequestParam Long buildingId
    ) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO, buildingId));
    }


    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/getroombyid/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/getallrooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/activerooms")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/roomsinbuilding/{buildingId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(roomService.getRoomsByBuilding(buildingId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateroom/{id}")
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDTO roomDTO
    ) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<RoomDTO> activateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.activateRoom(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<RoomDTO> deactivateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.deactivateRoom(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/Roomfeatures")
    public ResponseEntity<?> getRoomFeatures(@PathVariable Long id) {
        try {
            Set<RoomFeature> features = roomService.getFeaturesByRoomId(id);
            return ResponseEntity.ok(features);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/room/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
