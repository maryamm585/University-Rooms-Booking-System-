package com.university.booking.service;

import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.Building;
import com.university.booking.entity.Room;
import com.university.booking.entity.RoomFeature;
import com.university.booking.mapper.RoomMapper;
import com.university.booking.repository.BuildingRepository;
import com.university.booking.repository.RoomFeatureRepository;
import com.university.booking.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepo;
    private final BuildingRepository buildingRepo;
    private final RoomFeatureRepository featureRepo;

    public RoomService(RoomRepository roomRepo,
                       BuildingRepository buildingRepo,
                       RoomFeatureRepository featureRepo) {
        this.roomRepo = roomRepo;
        this.buildingRepo = buildingRepo;
        this.featureRepo = featureRepo;
    }

    public RoomDTO createRoom(RoomDTO dto, Long buildingId) {
        Building building = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building not found"));

        Room room = RoomMapper.toEntity(dto);
        room.setBuilding(building);
        room.setFeatures(resolveFeatures(dto.getFeatureIds()));
        return RoomMapper.toDTO(roomRepo.save(room));
    }
    public Set<RoomFeature> getFeaturesByRoomId(Long roomId) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + roomId));

        return room.getFeatures();
    }

    public RoomDTO getRoomById(Long id) {
        return RoomMapper.toDTO(getRoomEntityById(id));
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepo.findAll().stream()
                .map(RoomMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getActiveRooms() {
        return roomRepo.findByActiveTrue().stream()
                .map(RoomMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getRoomsByBuilding(Long buildingId) {
        return roomRepo.findByBuildingIdAndBuilding_ActiveTrue(buildingId).stream()
                .map(RoomMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room existing = getRoomEntityById(id);
        existing.setName(dto.getName());
        existing.setCapacity(dto.getCapacity());
        existing.setDescription(dto.getDescription());
        existing.setActive(dto.getActive());

        if (dto.getBuildingId() != null && (existing.getBuilding() == null
                || !dto.getBuildingId().equals(existing.getBuilding().getId()))) {
            Building newBuilding = buildingRepo.findById(dto.getBuildingId())
                    .orElseThrow(() -> new EntityNotFoundException("Building not found"));
            existing.setBuilding(newBuilding);
        }

        existing.setFeatures(resolveFeatures(dto.getFeatureIds()));

        return RoomMapper.toDTO(roomRepo.save(existing));
    }

    public RoomDTO activateRoom(Long id) {
        Room room = getRoomEntityById(id);
        room.setActive(true);
        return RoomMapper.toDTO(roomRepo.save(room));
    }

    public RoomDTO deactivateRoom(Long id) {
        Room room = getRoomEntityById(id);
        room.setActive(false);
        return RoomMapper.toDTO(roomRepo.save(room));
    }

    public void deleteRoom(Long id) {
        if (roomRepo.existsById(id)) {
            roomRepo.deleteById(id);
        } else {
            throw new RuntimeException("There is no room with id: " + id);
        }
    }

    private Room getRoomEntityById(Long id) {
        return roomRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    }

    private Set<RoomFeature> resolveFeatures(Set<Long> featureIds) {
        if (featureIds == null || featureIds.isEmpty()) return new HashSet<>();
        List<RoomFeature> found = featureRepo.findAllById(featureIds);

        if (found.size() != featureIds.size()) {
            throw new EntityNotFoundException("No RoomFeature ID");
        }
        return new HashSet<>(found);
    }
}
