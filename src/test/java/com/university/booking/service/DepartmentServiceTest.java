package com.university.booking.service;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.entity.Department;
import com.university.booking.entity.Role;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department testDepartment1;
    private Department testDepartment2;
    private Department testDepartment3;
    private DepartmentDTO testDepartmentDTO;
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

        // Create test departments
        testDepartment1 = new Department();
        testDepartment1.setId(1L);
        testDepartment1.setName("Computer Science");
        testDepartment1.setCode("CS");
        testDepartment1.setDescription("Department of Computer Science and Engineering");
        testDepartment1.setActive(true);

        testDepartment2 = new Department();
        testDepartment2.setId(2L);
        testDepartment2.setName("Mathematics");
        testDepartment2.setCode("MATH");
        testDepartment2.setDescription("Department of Mathematics");
        testDepartment2.setActive(true);

        testDepartment3 = new Department();
        testDepartment3.setId(3L);
        testDepartment3.setName("Physics");
        testDepartment3.setCode("PHY");
        testDepartment3.setDescription("Department of Physics - Inactive");
        testDepartment3.setActive(false);

        // Create test DepartmentDTO
        testDepartmentDTO = new DepartmentDTO();
        testDepartmentDTO.setId(1L);
        testDepartmentDTO.setName("Computer Science");
        testDepartmentDTO.setCode("CS");
        testDepartmentDTO.setDescription("Department of Computer Science and Engineering");
        testDepartmentDTO.setActive(true);
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
    void testCreateDepartmentSuccess() {
        setupSecurityContext();

        when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment1);

        DepartmentDTO result = departmentService.createDepartment(testDepartmentDTO);

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        assertEquals("CS", result.getCode());
        assertEquals("Department of Computer Science and Engineering", result.getDescription());
        assertTrue(result.getActive());

        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testCreateDepartmentUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.createDepartment(testDepartmentDTO));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testGetDepartmentByIdSuccess() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        DepartmentDTO result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Computer Science", result.getName());
        assertEquals("CS", result.getCode());
        assertEquals("Department of Computer Science and Engineering", result.getDescription());
        assertTrue(result.getActive());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDepartmentByIdNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.getDepartmentById(999L));
        assertEquals("No department with id: 999", exception.getMessage());
        verify(departmentRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllDepartments() {
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2, testDepartment3);
        when(departmentRepository.findAll()).thenReturn(departments);

        List<DepartmentDTO> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Computer Science", result.get(0).getName());
        assertEquals("Mathematics", result.get(1).getName());
        assertEquals("Physics", result.get(2).getName());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetAllDepartmentsEmptyList() {
        when(departmentRepository.findAll()).thenReturn(Arrays.asList());

        List<DepartmentDTO> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetActiveDepartments() {
        List<Department> activeDepartments = Arrays.asList(testDepartment1, testDepartment2);
        when(departmentRepository.findByActiveTrue()).thenReturn(activeDepartments);

        List<DepartmentDTO> result = departmentService.getActiveDepartments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Computer Science", result.get(0).getName());
        assertEquals("Mathematics", result.get(1).getName());
        assertTrue(result.get(0).getActive());
        assertTrue(result.get(1).getActive());
        verify(departmentRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testGetActiveDepartmentsEmptyList() {
        when(departmentRepository.findByActiveTrue()).thenReturn(Arrays.asList());

        List<DepartmentDTO> result = departmentService.getActiveDepartments();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(departmentRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testUpdateDepartmentSuccess() {
        setupSecurityContext();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        DepartmentDTO updatedDTO = new DepartmentDTO();
        updatedDTO.setName("Updated Computer Science");
        updatedDTO.setCode("UPD-CS");
        updatedDTO.setDescription("Updated description");
        updatedDTO.setActive(false);

        Department updatedDepartment = new Department();
        updatedDepartment.setId(1L);
        updatedDepartment.setName("Updated Computer Science");
        updatedDepartment.setCode("UPD-CS");
        updatedDepartment.setDescription("Updated description");
        updatedDepartment.setActive(false);

        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        DepartmentDTO result = departmentService.updateDepartment(1L, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Computer Science", result.getName());
        assertEquals("UPD-CS", result.getCode());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getActive());

        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateDepartmentNotFound() {
        setupSecurityContext();

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        DepartmentDTO updatedDTO = new DepartmentDTO();
        updatedDTO.setName("Updated Department");
        updatedDTO.setCode("UPD");

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.updateDepartment(999L, updatedDTO));
        assertEquals("No department with id: 999", exception.getMessage());

        verify(departmentRepository, times(1)).findById(999L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testUpdateDepartmentUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        DepartmentDTO updatedDTO = new DepartmentDTO();
        updatedDTO.setName("Updated Department");
        updatedDTO.setCode("UPD");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(1L, updatedDTO));
        verify(departmentRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testActivateDepartmentSuccess() {
        setupSecurityContext();

        testDepartment1.setActive(false); // Start with inactive department
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        Department activatedDepartment = new Department();
        activatedDepartment.setId(1L);
        activatedDepartment.setName("Computer Science");
        activatedDepartment.setCode("CS");
        activatedDepartment.setDescription("Department of Computer Science and Engineering");
        activatedDepartment.setActive(true);

        when(departmentRepository.save(any(Department.class))).thenReturn(activatedDepartment);

        DepartmentDTO result = departmentService.activateDepartment(1L);

        assertNotNull(result);
        assertTrue(result.getActive());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testActivateDepartmentNotFound() {
        setupSecurityContext();

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.activateDepartment(999L));
        assertEquals("No department with id: 999", exception.getMessage());

        verify(departmentRepository, times(1)).findById(999L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testActivateDepartmentUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.activateDepartment(1L));
        verify(departmentRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testDeactivateDepartmentSuccess() {
        setupSecurityContext();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        Department deactivatedDepartment = new Department();
        deactivatedDepartment.setId(1L);
        deactivatedDepartment.setName("Computer Science");
        deactivatedDepartment.setCode("CS");
        deactivatedDepartment.setDescription("Department of Computer Science and Engineering");
        deactivatedDepartment.setActive(false);

        when(departmentRepository.save(any(Department.class))).thenReturn(deactivatedDepartment);

        DepartmentDTO result = departmentService.deactivateDepartment(1L);

        assertNotNull(result);
        assertFalse(result.getActive());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeactivateDepartmentNotFound() {
        setupSecurityContext();

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.deactivateDepartment(999L));
        assertEquals("No department with id: 999", exception.getMessage());

        verify(departmentRepository, times(1)).findById(999L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testDeactivateDepartmentUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deactivateDepartment(1L));
        verify(departmentRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testDeleteDepartmentSuccess() {
        setupSecurityContext();

        when(departmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(departmentRepository).deleteById(1L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).existsById(1L);
        verify(departmentRepository, times(1)).deleteById(1L);
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testDeleteDepartmentNotFound() {
        setupSecurityContext();

        when(departmentRepository.existsById(999L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.deleteDepartment(999L));
        assertEquals("No department with id: 999", exception.getMessage());

        verify(departmentRepository, times(1)).existsById(999L);
        verify(departmentRepository, never()).deleteById(anyLong());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testDeleteDepartmentUserNotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(1L));
        verify(departmentRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(departmentRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetDepartmentEntityByIdPrivateMethodBehavior() {
        // Test the behavior through public methods that use the private method
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        DepartmentDTO result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateDepartmentWithNullValues() {
        setupSecurityContext();

        DepartmentDTO nullDTO = new DepartmentDTO();
        nullDTO.setName(null);
        nullDTO.setCode(null);
        nullDTO.setDescription(null);
        nullDTO.setActive(null);

        Department nullDepartment = new Department();
        nullDepartment.setId(1L);
        nullDepartment.setName(null);
        nullDepartment.setCode(null);
        nullDepartment.setDescription(null);
        nullDepartment.setActive(null);

        when(departmentRepository.save(any(Department.class))).thenReturn(nullDepartment);

        DepartmentDTO result = departmentService.createDepartment(nullDTO);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getCode());
        assertNull(result.getDescription());
        assertNull(result.getActive());

        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }

    @Test
    void testUpdateDepartmentWithNullValues() {
        setupSecurityContext();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment1));

        DepartmentDTO nullDTO = new DepartmentDTO();
        nullDTO.setName(null);
        nullDTO.setCode(null);
        nullDTO.setDescription(null);
        nullDTO.setActive(null);

        Department updatedDepartment = new Department();
        updatedDepartment.setId(1L);
        updatedDepartment.setName(null);
        updatedDepartment.setCode(null);
        updatedDepartment.setDescription(null);
        updatedDepartment.setActive(null);

        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        DepartmentDTO result = departmentService.updateDepartment(1L, nullDTO);

        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getCode());
        assertNull(result.getDescription());
        assertNull(result.getActive());

        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(userRepository, times(1)).findByEmail("admin@example.com");
    }
}