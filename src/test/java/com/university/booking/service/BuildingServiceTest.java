package com.university.booking.service;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.entity.Building;
import com.university.booking.entity.Role;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.BuildingRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BuildingService buildingService;

    private Building testBuilding1;
    private Building testBuilding2;
    private Building testBuilding3;
    private BuildingDTO testBuildingDTO;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        // Create authenticated user
        authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setFirstName("Admin");
        authenticatedUser.setLastName("User");
        authenticatedUser.setEmail("admin@example.com");
        authenticatedUser.setRole(Role.ADMIN);

        // Create test buildings
        testBuilding1 = new Building();
        testBuilding1.setId(1L);
        testBuilding1.setName("Engineering Building");
        testBuilding1.setCode("ENG");
        testBuilding1.setAddress("123 University Ave");
        testBuilding1.setDescription("Main engineering building");
        testBuilding1.setActive(true);

        testBuilding2 = new Building();
        testBuilding2.setId(2L);
        testBuilding2.setName("Science Building");
        testBuilding2.setCode("SCI");
        testBuilding2.setAddress("456 Campus Road");
        testBuilding2.setDescription("Science laboratories and classrooms");
        testBuilding2.setActive(true);

        testBuilding3 = new Building();
        testBuilding3.setId(3L);
        testBuilding3.setName("Old Library");
        testBuilding3.setCode("LIB-OLD");
        testBuilding3.setAddress("789 Library Lane");
        testBuilding3.setDescription("Historic library building");
        testBuilding3.setActive(false);

        // Create test BuildingDTO
        testBuildingDTO = new BuildingDTO();
        testBuildingDTO.setId(1L);
        testBuildingDTO.setName("Engineering Building");
        testBuildingDTO.setCode("ENG");
        testBuildingDTO.setAddress("123 University Ave");
        testBuildingDTO.setDescription("Main engineering building");
        testBuildingDTO.setActive(true);
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
    void testCreateBuildingSuccess() {
        setupSecurityContext();

        when(buildingRepository.save(any(Building.class))).thenReturn(testBuilding1);

        BuildingDTO result = buildingService.createBuilding(testBuildingDTO);

        assertNotNull(result);
        assertEquals("Engineering Building", result.getName());
        assertEquals("ENG", result.getCode());
        assertEquals("123 University Ave", result.getAddress());
        assertEquals("Main engineering building", result.getDescription());
        assertTrue(result.getActive());

        verify(buildingRepository, times(1)).save(any(Building.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testCreateBuildingUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> buildingService.createBuilding(testBuildingDTO));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testActivateBuildingSuccess() {
        setupSecurityContext();

        testBuilding1.setActive(false); // Start with inactive building
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));

        Building activatedBuilding = new Building();
        activatedBuilding.setId(1L);
        activatedBuilding.setName("Engineering Building");
        activatedBuilding.setCode("ENG");
        activatedBuilding.setAddress("123 University Ave");
        activatedBuilding.setDescription("Main engineering building");
        activatedBuilding.setActive(true);

        when(buildingRepository.save(any(Building.class))).thenReturn(activatedBuilding);

        BuildingDTO result = buildingService.activateBuilding(1L);

        assertNotNull(result);
        assertTrue(result.getActive());
        verify(buildingRepository, times(1)).findById(1L);
        verify(buildingRepository, times(1)).save(any(Building.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testActivateBuildingNotFound() {
        setupSecurityContext();

        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> buildingService.activateBuilding(999L));
        verify(buildingRepository, times(1)).findById(999L);
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testDeactivateBuildingSuccess() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));

        Building deactivatedBuilding = new Building();
        deactivatedBuilding.setId(1L);
        deactivatedBuilding.setName("Engineering Building");
        deactivatedBuilding.setCode("ENG");
        deactivatedBuilding.setAddress("123 University Ave");
        deactivatedBuilding.setDescription("Main engineering building");
        deactivatedBuilding.setActive(false);

        when(buildingRepository.save(any(Building.class))).thenReturn(deactivatedBuilding);

        BuildingDTO result = buildingService.deactivateBuilding(1L);

        assertNotNull(result);
        assertFalse(result.getActive());
        verify(buildingRepository, times(1)).findById(1L);
        verify(buildingRepository, times(1)).save(any(Building.class));
    }

    @Test
    void testDeactivateBuildingNotFound() {
        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> buildingService.deactivateBuilding(999L));
        verify(buildingRepository, times(1)).findById(999L);
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testGetBuildingByIdSuccess() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));

        BuildingDTO result = buildingService.getBuildingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering Building", result.getName());
        assertEquals("ENG", result.getCode());
        verify(buildingRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBuildingByIdNotFound() {
        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> buildingService.getBuildingById(999L));
        verify(buildingRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllBuildings() {
        List<Building> buildings = Arrays.asList(testBuilding1, testBuilding2, testBuilding3);
        when(buildingRepository.findAll()).thenReturn(buildings);

        List<BuildingDTO> result = buildingService.getAllBuildings();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Engineering Building", result.get(0).getName());
        assertEquals("Science Building", result.get(1).getName());
        assertEquals("Old Library", result.get(2).getName());
        verify(buildingRepository, times(1)).findAll();
    }

    @Test
    void testGetAllActiveBuildings() {
        List<Building> activeBuildings = Arrays.asList(testBuilding1, testBuilding2);
        when(buildingRepository.findByActiveTrue()).thenReturn(activeBuildings);

        List<BuildingDTO> result = buildingService.getAllActiveBuildings();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Engineering Building", result.get(0).getName());
        assertEquals("Science Building", result.get(1).getName());
        assertTrue(result.get(0).getActive());
        assertTrue(result.get(1).getActive());
        verify(buildingRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testUpdateBuildingSuccess() {
        setupSecurityContext();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));

        BuildingDTO updatedDTO = new BuildingDTO();
        updatedDTO.setName("Updated Engineering Building");
        updatedDTO.setCode("UPD-ENG");
        updatedDTO.setAddress("999 Updated Ave");
        updatedDTO.setDescription("Updated description");
        updatedDTO.setActive(false);

        Building updatedBuilding = new Building();
        updatedBuilding.setId(1L);
        updatedBuilding.setName("Updated Engineering Building");
        updatedBuilding.setCode("UPD-ENG");
        updatedBuilding.setAddress("999 Updated Ave");
        updatedBuilding.setDescription("Updated description");
        updatedBuilding.setActive(false);

        when(buildingRepository.save(any(Building.class))).thenReturn(updatedBuilding);

        BuildingDTO result = buildingService.updateBuilding(1L, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Engineering Building", result.getName());
        assertEquals("UPD-ENG", result.getCode());
        assertEquals("999 Updated Ave", result.getAddress());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getActive());

        verify(buildingRepository, times(1)).findById(1L);
        verify(buildingRepository, times(1)).save(any(Building.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateBuildingNotFound() {
        setupSecurityContext();

        when(buildingRepository.findById(999L)).thenReturn(Optional.empty());

        BuildingDTO updatedDTO = new BuildingDTO();
        updatedDTO.setName("Updated Building");
        updatedDTO.setCode("UPD");

        assertThrows(EntityNotFoundException.class, () -> buildingService.updateBuilding(999L, updatedDTO));
        verify(buildingRepository, times(1)).findById(999L);
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testUpdateBuildingUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BuildingDTO updatedDTO = new BuildingDTO();
        updatedDTO.setName("Updated Building");
        updatedDTO.setCode("UPD");

        assertThrows(ResourceNotFoundException.class, () -> buildingService.updateBuilding(1L, updatedDTO));
        verify(buildingRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testDeleteBuildingSuccess() {
        setupSecurityContext();

        doNothing().when(buildingRepository).deleteById(1L);

        buildingService.deleteBuilding(1L);

        verify(buildingRepository, times(1)).deleteById(1L);
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeleteBuildingUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> buildingService.deleteBuilding(1L));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(buildingRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetBuildingEntityByIdPrivateMethodBehavior() {
        // Test the behavior through public methods that use the private method
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));

        BuildingDTO result = buildingService.getBuildingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(buildingRepository, times(1)).findById(1L);
    }

    @Test
    void testActivateBuildingWithUserNotFoundDuringLogging() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> buildingService.activateBuilding(1L));
        verify(buildingRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testGetAllBuildingsEmptyList() {
        when(buildingRepository.findAll()).thenReturn(Arrays.asList());

        List<BuildingDTO> result = buildingService.getAllBuildings();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(buildingRepository, times(1)).findAll();
    }

    @Test
    void testGetAllActiveBuildingsEmptyList() {
        when(buildingRepository.findByActiveTrue()).thenReturn(Arrays.asList());

        List<BuildingDTO> result = buildingService.getAllActiveBuildings();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(buildingRepository, times(1)).findByActiveTrue();
    }
}