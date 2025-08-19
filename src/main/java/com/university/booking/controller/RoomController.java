package com.university.booking.controller;

import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.RoomFeature;
import com.university.booking.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/ADMIN/createroom")
    public ResponseEntity<RoomDTO> createRoom(
            @RequestBody RoomDTO roomDTO,
            @RequestParam Long buildingId
    ) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO, buildingId));
    }


    @GetMapping("/All/getroombyid/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/All/getallrooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/All/activerooms")
    public ResponseEntity<List<RoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }

    @GetMapping("/All/roomsinbuilding/{buildingId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(roomService.getRoomsByBuilding(buildingId));
    }

    @PutMapping("/ADMIN/updateroom/{id}")
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDTO roomDTO
    ) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @PutMapping("/ADMIN/activate/{id}")
    public ResponseEntity<RoomDTO> activateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.activateRoom(id));
    }

    @PutMapping("/ADMIN/deactivate/{id}")
    public ResponseEntity<RoomDTO> deactivateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.deactivateRoom(id));
    }
    @GetMapping("/ADMIN/{id}/Roomfeatures")
    public ResponseEntity<?> getRoomFeatures(@PathVariable Long id) {
        try {
            Set<RoomFeature> features = roomService.getFeaturesByRoomId(id);
            return ResponseEntity.ok(features);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/ADMIN/deleteroom/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
