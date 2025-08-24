package com.university.booking.service;

import com.university.booking.dto.request.UserCreateRequest;
import com.university.booking.dto.response.UserResponse;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.entity.Role;
import com.university.booking.entity.User;
import com.university.booking.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserCreateRequest request;

    @BeforeEach
    void setUp() {
        // Sample authenticated user
        user = new User();
        user.setId(1L);
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);

        // Request DTO
        request = new UserCreateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setRole(Role.STUDENT);
    }

    private void setupSecurityContext() {
        // Mock Authentication and SecurityContext
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.lenient().when(authentication.getName()).thenReturn("admin@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        // Mock SecurityContextHolder static
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Mock user repository to return authenticated user
        Mockito.lenient().when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        // Clean up the static mock after each test
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
            mockedSecurityContextHolder = null;
        }
    }

    @Test
    void shouldCreateUserSuccessfully() {
        setupSecurityContext(); // UserService.createUser() uses SecurityContext

        Mockito.when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());
    }

    @Test
    void shouldThrowIfEmailAlreadyExists() {
        setupSecurityContext(); // UserService.createUser() uses SecurityContext

        Mockito.when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(request));

        assertEquals("Error: Email is already in use!", ex.getMessage());
    }

    // ============================
    // Test: Get User by ID
    // ============================
    @Test
    void shouldReturnUserById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void shouldThrowIfUserNotFoundById() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(2L));
    }

    // ============================
    // Test: Get All Users
    // ============================
    @Test
    void shouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("alice@example.com");
        user2.setRole(Role.FACULTY_MEMBER);

        Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserResponse> users = userService.getAllUsers();
        assertEquals(2, users.size());
    }

    // ============================
    // Test: Delete User
    // ============================
    @Test
    void shouldDeleteUserSuccessfully() {
        // Create test user
        User userToDelete = new User();
        userToDelete.setId(999L);
        userToDelete.setFirstName("Test");
        userToDelete.setLastName("User");
        userToDelete.setEmail("test999@example.com");
        userToDelete.setRole(Role.STUDENT);

        // Mock the repository call
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.of(userToDelete));

        // Debug: Check if the service is using the same repository instance
        System.out.println("Mock repository: " + userRepository.hashCode());

        // Test the mock directly
        Optional<User> testResult = userRepository.findById(999L);
        System.out.println("Direct mock test - User found: " + testResult.isPresent());

        try {
            // Execute the service method
            userService.deleteUser(999L);
            System.out.println("Delete operation completed successfully");
        } catch (ResourceNotFoundException e) {
            System.out.println("Service failed with ResourceNotFoundException: " + e.getMessage());

            // Let's try to understand why by checking what the service actually gets
            Optional<User> serviceResult = userRepository.findById(999L);
            System.out.println("Repository call from test context - User found: " + serviceResult.isPresent());

            // The issue might be that the service method has some logic we're not seeing
            // or it's calling a different method on the repository
            fail("Service method failed despite mock being set up correctly");
        }

        // If we get here, verify the interactions
        Mockito.verify(userRepository).findById(999L);
        Mockito.verify(userRepository).delete(userToDelete);
    }

    @Test
    void shouldThrowIfDeletingNonExistentUser() {
        Mockito.lenient().when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(2L));
    }
}