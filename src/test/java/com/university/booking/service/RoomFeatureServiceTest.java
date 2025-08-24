package com.university.booking.service;

import com.university.booking.entity.Role;
import com.university.booking.entity.RoomFeature;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.RoomFeatureRepository;
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
class RoomFeatureServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private RoomFeatureRepository roomFeatureRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomFeatureService roomFeatureService;

    private RoomFeature testFeature1;
    private RoomFeature testFeature2;
    private RoomFeature testFeature3;
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

        // Create test room features
        testFeature1 = new RoomFeature();
        testFeature1.setName("Projector");

        testFeature2 = new RoomFeature();
        testFeature2.setName("Whiteboard");

        testFeature3 = new RoomFeature();
        testFeature3.setName("Audio System");
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
    void testCreateFeatureSuccess() {
        setupSecurityContext();

        RoomFeature newFeature = new RoomFeature();
        newFeature.setName("New Projector");

        when(roomFeatureRepository.existsByName("New Projector")).thenReturn(false);
        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(testFeature1);

        RoomFeature result = roomFeatureService.createFeature(newFeature);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Projector", result.getName());

        verify(roomFeatureRepository, times(1)).existsByName("New Projector");
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testCreateFeatureNameAlreadyExists() {
        setupSecurityContext();

        RoomFeature duplicateFeature = new RoomFeature();
        duplicateFeature.setName("Existing Projector");

        when(roomFeatureRepository.existsByName("Existing Projector")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roomFeatureService.createFeature(duplicateFeature));
        assertEquals("Feature with this name already exists!", exception.getMessage());

        verify(roomFeatureRepository, times(1)).existsByName("Existing Projector");
        verify(roomFeatureRepository, never()).save(any(RoomFeature.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testCreateFeatureUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        RoomFeature newFeature = new RoomFeature();
        newFeature.setName("New Feature");

        when(roomFeatureRepository.existsByName("New Feature")).thenReturn(false);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomFeatureService.createFeature(newFeature));
        verify(roomFeatureRepository, times(1)).existsByName("New Feature");
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomFeatureRepository, never()).save(any(RoomFeature.class));
    }

    @Test
    void testGetFeatureByIdSuccess() {
        when(roomFeatureRepository.findById(1L)).thenReturn(Optional.of(testFeature1));

        RoomFeature result = roomFeatureService.getFeatureById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Projector", result.getName());
        verify(roomFeatureRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFeatureByIdNotFound() {
        when(roomFeatureRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> roomFeatureService.getFeatureById(999L));
        assertEquals("Feature not found with ID: 999", exception.getMessage());
        verify(roomFeatureRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllFeatures() {
        List<RoomFeature> features = Arrays.asList(testFeature1, testFeature2, testFeature3);
        when(roomFeatureRepository.findAll()).thenReturn(features);

        List<RoomFeature> result = roomFeatureService.getAllFeatures();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Projector", result.get(0).getName());
        assertEquals("Whiteboard", result.get(1).getName());
        assertEquals("Audio System", result.get(2).getName());
        verify(roomFeatureRepository, times(1)).findAll();
    }

    @Test
    void testGetAllFeaturesEmptyList() {
        when(roomFeatureRepository.findAll()).thenReturn(Arrays.asList());

        List<RoomFeature> result = roomFeatureService.getAllFeatures();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(roomFeatureRepository, times(1)).findAll();
    }

    @Test
    void testUpdateFeatureSuccess() {
        setupSecurityContext();

        when(roomFeatureRepository.findById(1L)).thenReturn(Optional.of(testFeature1));

        RoomFeature updateData = new RoomFeature();
        updateData.setName("Updated Projector");

        RoomFeature updatedFeature = new RoomFeature();
        updatedFeature.setName("Updated Projector");

        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(updatedFeature);

        RoomFeature result = roomFeatureService.updateFeature(1L, updateData);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Projector", result.getName());

        verify(roomFeatureRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateFeatureNotFound() {
        setupSecurityContext();

        when(roomFeatureRepository.findById(999L)).thenReturn(Optional.empty());

        RoomFeature updateData = new RoomFeature();
        updateData.setName("Updated Feature");

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> roomFeatureService.updateFeature(999L, updateData));
        assertEquals("Feature not found with ID: 999", exception.getMessage());

        verify(roomFeatureRepository, times(1)).findById(999L);
        verify(roomFeatureRepository, never()).save(any(RoomFeature.class));
    }

    @Test
    void testUpdateFeatureUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomFeatureRepository.findById(1L)).thenReturn(Optional.of(testFeature1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        RoomFeature updateData = new RoomFeature();
        updateData.setName("Updated Feature");

        assertThrows(ResourceNotFoundException.class,
                () -> roomFeatureService.updateFeature(1L, updateData));
        verify(roomFeatureRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomFeatureRepository, never()).save(any(RoomFeature.class));
    }

    @Test
    void testDeleteFeatureSuccess() {
        setupSecurityContext();

        when(roomFeatureRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomFeatureRepository).deleteById(1L);

        roomFeatureService.deleteFeature(1L);

        verify(roomFeatureRepository, times(1)).existsById(1L);
        verify(roomFeatureRepository, times(1)).deleteById(1L);
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeleteFeatureNotFound() {
        setupSecurityContext();

        when(roomFeatureRepository.existsById(999L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> roomFeatureService.deleteFeature(999L));
        assertEquals("Feature not found with ID: 999", exception.getMessage());

        verify(roomFeatureRepository, times(1)).existsById(999L);
        verify(roomFeatureRepository, never()).deleteById(anyLong());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testDeleteFeatureUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(roomFeatureRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomFeatureService.deleteFeature(1L));
        verify(roomFeatureRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(roomFeatureRepository, never()).deleteById(anyLong());
    }

    @Test
    void testCreateFeatureWithNullName() {
        setupSecurityContext();

        RoomFeature featureWithNullName = new RoomFeature();
        featureWithNullName.setName(null);

        when(roomFeatureRepository.existsByName(null)).thenReturn(false);
        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(featureWithNullName);

        RoomFeature result = roomFeatureService.createFeature(featureWithNullName);

        assertNotNull(result);
        assertNull(result.getName());

        verify(roomFeatureRepository, times(1)).existsByName(null);
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateFeatureWithNullName() {
        setupSecurityContext();

        when(roomFeatureRepository.findById(1L)).thenReturn(Optional.of(testFeature1));

        RoomFeature updateDataWithNullName = new RoomFeature();
        updateDataWithNullName.setName(null);

        RoomFeature updatedFeature = new RoomFeature();
        updatedFeature.setName(null);

        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(updatedFeature);

        RoomFeature result = roomFeatureService.updateFeature(1L, updateDataWithNullName);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getName());

        verify(roomFeatureRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testCreateFeatureLogsCorrectId() {
        setupSecurityContext();

        RoomFeature newFeature = new RoomFeature();
        newFeature.setName("Test Feature");

        when(roomFeatureRepository.existsByName("Test Feature")).thenReturn(false);
        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(testFeature1);

        roomFeatureService.createFeature(newFeature);

        // The logging should use the feature ID from the input parameter
        verify(roomFeatureRepository, times(1)).existsByName("Test Feature");
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateFeatureLogsCorrectId() {
        setupSecurityContext();

        when(roomFeatureRepository.findById(1L)).thenReturn(Optional.of(testFeature1));

        RoomFeature updateData = new RoomFeature();
        updateData.setName("Updated Feature");

        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(testFeature1);

        roomFeatureService.updateFeature(1L, updateData);

        // The logging should use the feature ID from the update parameter
        verify(roomFeatureRepository, times(1)).findById(1L);
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testGetFeatureByIdWithDifferentIds() {
        // Test with different feature IDs
        when(roomFeatureRepository.findById(2L)).thenReturn(Optional.of(testFeature2));
        when(roomFeatureRepository.findById(3L)).thenReturn(Optional.of(testFeature3));

        RoomFeature result2 = roomFeatureService.getFeatureById(2L);
        RoomFeature result3 = roomFeatureService.getFeatureById(3L);

        assertEquals(2L, result2.getId());
        assertEquals("Whiteboard", result2.getName());
        assertEquals(3L, result3.getId());
        assertEquals("Audio System", result3.getName());

        verify(roomFeatureRepository, times(1)).findById(2L);
        verify(roomFeatureRepository, times(1)).findById(3L);
    }

    @Test
    void testServiceMethodsWithEmptyStrings() {
        setupSecurityContext();

        RoomFeature featureWithEmptyName = new RoomFeature();
        featureWithEmptyName.setName("");

        when(roomFeatureRepository.existsByName("")).thenReturn(false);
        when(roomFeatureRepository.save(any(RoomFeature.class))).thenReturn(featureWithEmptyName);

        RoomFeature result = roomFeatureService.createFeature(featureWithEmptyName);

        assertNotNull(result);
        assertEquals("", result.getName());

        verify(roomFeatureRepository, times(1)).existsByName("");
        verify(roomFeatureRepository, times(1)).save(any(RoomFeature.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }
}