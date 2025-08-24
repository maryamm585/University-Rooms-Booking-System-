package com.university.booking.service;

import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.Building;
import com.university.booking.entity.Room;
import com.university.booking.entity.RoomFeature;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.mapper.RoomMapper;
import com.university.booking.repository.BuildingRepository;
import com.university.booking.repository.RoomFeatureRepository;
import com.university.booking.repository.RoomRepository;
import com.university.booking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepo;
    private final BuildingRepository buildingRepo;
    private final RoomFeatureRepository featureRepo;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(RoomFeatureService.class);

    public RoomDTO createRoom(RoomDTO dto, Long buildingId) {
        Building building = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building not found"));

        Room room = RoomMapper.toEntity(dto);
        room.setBuilding(building);
        room.setFeatures(resolveFeatures(dto.getFeatureIds()));

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A New Room with id {} was Created at {} by user with id {} and role {}",
                room.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

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

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Room with id {} was Updated at {} by user with id {} and role {}",
                existing.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return RoomMapper.toDTO(roomRepo.save(existing));
    }

    public RoomDTO activateRoom(Long id) {
        Room room = getRoomEntityById(id);
        room.setActive(true);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Room with id {} was Activated at {} by user with id {} and role {}",
                room.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return RoomMapper.toDTO(roomRepo.save(room));
    }

    public RoomDTO deactivateRoom(Long id) {
        Room room = getRoomEntityById(id);
        room.setActive(false);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Room with id {} was Deactivated at {} by user with id {} and role {}",
                room.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return RoomMapper.toDTO(roomRepo.save(room));
    }

    public void deleteRoom(Long id) {
        if (roomRepo.existsById(id)) {
            roomRepo.deleteById(id);
            // get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User currentUser = userRepo.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

            logger.info("A Room with id {} was Deleted at {} by user with id {} and role {}",
                    id,
                    new Date(System.currentTimeMillis()),
                    currentUser.getId(),
                    currentUser.getRole().name()
            );
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
