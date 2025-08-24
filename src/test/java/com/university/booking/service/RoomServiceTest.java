package com.university.booking.service;

import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.Building;
import com.university.booking.entity.Role;
import com.university.booking.entity.Room;
import com.university.booking.entity.RoomFeature;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.BuildingRepository;
import com.university.booking.repository.RoomFeatureRepository;
import com.university.booking.repository.RoomRepository;
import com.university.booking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private RoomFeatureRepository roomFeatureRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom1;
    private Room testRoom2;
    private Room testRoom3;
    private RoomDTO testRoomDTO;
    private Building testBuilding;
    private User authenticatedUser;
    private RoomFeature testFeature1;
    private RoomFeature testFeature2;

    @BeforeEach
    void setUp() {
        // Create authenticated user
        authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setFirstName("Admin");
        authenticatedUser.setLastName("User");
        authenticatedUser.setEmail("admin@example.com");
        authenticatedUser.setRole(Role.ADMIN);

        // Create test building
        testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setName("Engineering Building");
        testBuilding.setCode("ENG");
        testBuilding.setActive(true);

        // Create test room features
        testFeature1 = new RoomFeature();
        testFeature1.setName("Projector");

        testFeature2 = new RoomFeature();
        testFeature2.setName("Whiteboard");

        // Create test rooms
        testRoom1 = new Room();
        testRoom1.setId(1L);
        testRoom1.setName("Room 101");
        testRoom1.setCapacity(30);
        testRoom1.setDescription("Lecture room with projector");
        testRoom1.setActive(true);
        testRoom1.setBuilding(testBuilding);
        testRoom1.setFeatures(Set.of(testFeature1));

        testRoom2 = new Room();
        testRoom2.setId(2L);
        testRoom2.setName("Room 102");
        testRoom2.setCapacity(25);
        testRoom2.setDescription("Seminar room");
        testRoom2.setActive(true);
        testRoom2.setBuilding(testBuilding);
        testRoom2.setFeatures(Set.of(testFeature2));

        testRoom3 = new Room();
        testRoom3.setId(3L);
        testRoom3.setName("Room 103");
        testRoom3.setCapacity(20);
        testRoom3.setDescription("Small meeting room");
        testRoom3.setActive(false);
        testRoom3.setBuilding(testBuilding);
        testRoom3.setFeatures(new HashSet<>());

        // Create test RoomDTO
        testRoomDTO = new RoomDTO();
        testRoomDTO.setId(1L);
        testRoomDTO.setName("Room 101");
        testRoomDTO.setCapacity(30);
        testRoomDTO.setDescription("Lecture room with projector");
        testRoomDTO.setActive(true);
        testRoomDTO.setBuildingId(1L);
        testRoomDTO.setFeatureIds(Set.of(1L));
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
            mockedSecurityContextHolder = null;
        }
    }

    private void setupSecurityContext() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(authenticatedUser));
    }

    @Test
    void testCreateRoomSuccess() {
        setupSecurityContext();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(roomFeatureRepository.findAllById(Set.of(1L))).thenReturn(Arrays.asList(testFeature1));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom1);

        RoomDTO result = roomService.createRoom(testRoomDTO, 1L);

        assertNotNull(result);
        assertEquals("Room 101", result.getName());
        assertEquals(30, result.getCapacity());
        assertEquals("Lecture room with projector", result.getDescription());
        assertTrue(result.getActive());
        assertEquals(1L, result.getBuildingId());

        verify(buildingRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).findAllById(Set.of(1L));
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testCreateRoomBuildingNotFound() {
        setupSecurityContext();

        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.createRoom(testRoomDTO, 999L));
        verify(buildingRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testCreateRoomUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(roomFeatureRepository.findAllById(Set.of(1L))).thenReturn(Arrays.asList(testFeature1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.createRoom(testRoomDTO, 1L));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testCreateRoomWithInvalidFeatures() {
        setupSecurityContext();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(roomFeatureRepository.findAllById(Set.of(1L, 999L))).thenReturn(Arrays.asList(testFeature1)); // Only returns 1 out of 2

        testRoomDTO.setFeatureIds(Set.of(1L, 999L));

        assertThrows(EntityNotFoundException.class, () -> roomService.createRoom(testRoomDTO, 1L));
        verify(buildingRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).findAllById(Set.of(1L, 999L));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testGetFeaturesByRoomIdSuccess() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));

        Set<RoomFeature> result = roomService.getFeaturesByRoomId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testFeature1));
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFeaturesByRoomIdNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.getFeaturesByRoomId(999L));
        verify(roomRepository, times(1)).findById(999L);
    }

    @Test
    void testGetRoomByIdSuccess() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));

        RoomDTO result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Room 101", result.getName());
        assertEquals(30, result.getCapacity());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRoomByIdNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.getRoomById(999L));
        verify(roomRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllRooms() {
        List<Room> rooms = Arrays.asList(testRoom1, testRoom2, testRoom3);
        when(roomRepository.findAll()).thenReturn(rooms);

        List<RoomDTO> result = roomService.getAllRooms();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Room 101", result.get(0).getName());
        assertEquals("Room 102", result.get(1).getName());
        assertEquals("Room 103", result.get(2).getName());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRoomsEmptyList() {
        when(roomRepository.findAll()).thenReturn(Arrays.asList());

        List<RoomDTO> result = roomService.getAllRooms();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void testGetActiveRooms() {
        List<Room> activeRooms = Arrays.asList(testRoom1, testRoom2);
        when(roomRepository.findByActiveTrue()).thenReturn(activeRooms);

        List<RoomDTO> result = roomService.getActiveRooms();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Room 101", result.get(0).getName());
        assertEquals("Room 102", result.get(1).getName());
        assertTrue(result.get(0).getActive());
        assertTrue(result.get(1).getActive());
        verify(roomRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testGetActiveRoomsEmptyList() {
        when(roomRepository.findByActiveTrue()).thenReturn(Arrays.asList());

        List<RoomDTO> result = roomService.getActiveRooms();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(roomRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testGetRoomsByBuilding() {
        List<Room> buildingRooms = Arrays.asList(testRoom1, testRoom2);
        when(roomRepository.findByBuildingIdAndBuilding_ActiveTrue(1L)).thenReturn(buildingRooms);

        List<RoomDTO> result = roomService.getRoomsByBuilding(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Room 101", result.get(0).getName());
        assertEquals("Room 102", result.get(1).getName());
        verify(roomRepository, times(1)).findByBuildingIdAndBuilding_ActiveTrue(1L);
    }

    @Test
    void testUpdateRoomSuccess() {
        setupSecurityContext();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(roomFeatureRepository.findAllById(Set.of(2L))).thenReturn(Arrays.asList(testFeature2));

        RoomDTO updatedDTO = new RoomDTO();
        updatedDTO.setName("Updated Room 101");
        updatedDTO.setCapacity(35);
        updatedDTO.setDescription("Updated description");
        updatedDTO.setActive(false);
        updatedDTO.setBuildingId(1L);
        updatedDTO.setFeatureIds(Set.of(2L));

        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setName("Updated Room 101");
        updatedRoom.setCapacity(35);
        updatedRoom.setDescription("Updated description");
        updatedRoom.setActive(false);
        updatedRoom.setBuilding(testBuilding);
        updatedRoom.setFeatures(Set.of(testFeature2));

        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        RoomDTO result = roomService.updateRoom(1L, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Room 101", result.getName());
        assertEquals(35, result.getCapacity());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getActive());

        verify(roomRepository, times(1)).findById(1L);
        verify(buildingRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).findAllById(Set.of(2L));
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateRoomNotFound() {
        setupSecurityContext();

        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        RoomDTO updatedDTO = new RoomDTO();
        updatedDTO.setName("Updated Room");

        assertThrows(EntityNotFoundException.class, () -> roomService.updateRoom(999L, updatedDTO));
        verify(roomRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testUpdateRoomBuildingNotFound() {
        setupSecurityContext();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        RoomDTO updatedDTO = new RoomDTO();
        updatedDTO.setName("Updated Room");
        updatedDTO.setBuildingId(999L);

        assertThrows(EntityNotFoundException.class, () -> roomService.updateRoom(1L, updatedDTO));
        verify(roomRepository, times(1)).findById(1L);
        verify(buildingRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testUpdateRoomUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        RoomDTO updatedDTO = new RoomDTO();
        updatedDTO.setName("Updated Room");

        assertThrows(ResourceNotFoundException.class, () -> roomService.updateRoom(1L, updatedDTO));
        verify(roomRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testActivateRoomSuccess() {
        setupSecurityContext();

        testRoom1.setActive(false); // Start with inactive room
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));

        Room activatedRoom = new Room();
        activatedRoom.setId(1L);
        activatedRoom.setName("Room 101");
        activatedRoom.setCapacity(30);
        activatedRoom.setDescription("Lecture room with projector");
        activatedRoom.setActive(true);
        activatedRoom.setBuilding(testBuilding);

        when(roomRepository.save(any(Room.class))).thenReturn(activatedRoom);

        RoomDTO result = roomService.activateRoom(1L);

        assertNotNull(result);
        assertTrue(result.getActive());
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testActivateRoomNotFound() {
        setupSecurityContext();

        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.activateRoom(999L));
        verify(roomRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testActivateRoomUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.activateRoom(1L));
        verify(roomRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testDeactivateRoomSuccess() {
        setupSecurityContext();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));

        Room deactivatedRoom = new Room();
        deactivatedRoom.setId(1L);
        deactivatedRoom.setName("Room 101");
        deactivatedRoom.setCapacity(30);
        deactivatedRoom.setDescription("Lecture room with projector");
        deactivatedRoom.setActive(false);
        deactivatedRoom.setBuilding(testBuilding);

        when(roomRepository.save(any(Room.class))).thenReturn(deactivatedRoom);

        RoomDTO result = roomService.deactivateRoom(1L);

        assertNotNull(result);
        assertFalse(result.getActive());
        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeactivateRoomNotFound() {
        setupSecurityContext();

        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.deactivateRoom(999L));
        verify(roomRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testDeactivateRoomUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.deactivateRoom(1L));
        verify(roomRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testDeleteRoomSuccess() {
        setupSecurityContext();

        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).existsById(1L);
        verify(roomRepository, times(1)).deleteById(1L);
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeleteRoomNotExists() {
        setupSecurityContext();

        when(roomRepository.existsById(999L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.deleteRoom(999L));
        assertEquals("There is no room with id: 999", exception.getMessage());

        verify(roomRepository, times(1)).existsById(999L);
        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteRoomUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(1L));
        verify(roomRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    void testCreateRoomWithNoFeatures() {
        setupSecurityContext();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom3);

        testRoomDTO.setFeatureIds(null);

        RoomDTO result = roomService.createRoom(testRoomDTO, 1L);

        assertNotNull(result);
        verify(buildingRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, never()).findAllById(anySet());
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateRoomWithNullBuildingId() {
        setupSecurityContext();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
        when(roomFeatureRepository.findAllById(Set.of())).thenReturn(Arrays.asList());

        RoomDTO updatedDTO = new RoomDTO();
        updatedDTO.setName("Updated Room 101");
        updatedDTO.setCapacity(35);
        updatedDTO.setDescription("Updated description");
        updatedDTO.setActive(true);
        updatedDTO.setBuildingId(null); // Null building ID
        updatedDTO.setFeatureIds(Set.of());

        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setName("Updated Room 101");
        updatedRoom.setCapacity(35);
        updatedRoom.setDescription("Updated description");
        updatedRoom.setActive(true);
        updatedRoom.setBuilding(testBuilding); // Should keep existing building

        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        RoomDTO result = roomService.updateRoom(1L, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Room 101", result.getName());
        verify(roomRepository, times(1)).findById(1L);
        verify(buildingRepository, never()).findById(anyLong());
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }
}