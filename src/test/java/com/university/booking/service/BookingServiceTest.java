package com.university.booking.service;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import com.university.booking.entity.*;
import com.university.booking.exception.BookingConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingHistoryRepository bookingHistoryRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking1;
    private Booking booking2;
    private User authenticatedUser;
    private User testUser1;
    private User testUser2;
    private Room testRoom1;
    private Room testRoom2;
    private Building testBuilding;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        // Create test building
        testBuilding = new Building();
        testBuilding.setId(1L);
        testBuilding.setName("Engineering Building");
        testBuilding.setCode("ENG");

        // Create test rooms
        testRoom1 = new Room();
        testRoom1.setId(201L);
        testRoom1.setName("Conference Room A");
        testRoom1.setRoomNumber("ENG-201");
        testRoom1.setBuilding(testBuilding);
        testRoom1.setCapacity(20);
        testRoom1.setDescription("Large conference room with projector");
        testRoom1.setActive(true);

        // Create room features
        RoomFeature projectorFeature = new RoomFeature("PROJECTOR");
        RoomFeature whiteboardFeature = new RoomFeature("WHITEBOARD");

        Set<RoomFeature> features1 = new HashSet<>();
        features1.add(projectorFeature);
        features1.add(whiteboardFeature);
        testRoom1.setFeatures(features1);

        testRoom2 = new Room();
        testRoom2.setId(202L);
        testRoom2.setName("Meeting Room B");
        testRoom2.setRoomNumber("ENG-202");
        testRoom2.setBuilding(testBuilding);
        testRoom2.setCapacity(10);
        testRoom2.setDescription("Small meeting room");
        testRoom2.setActive(true);

        Set<RoomFeature> features2 = new HashSet<>();
        features2.add(whiteboardFeature);
        testRoom2.setFeatures(features2);

        // Create test users
        testUser1 = new User();
        testUser1.setId(101L);
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setEmail("john.doe@example.com");
        testUser1.setRole(Role.STUDENT);

        testUser2 = new User();
        testUser2.setId(102L);
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setRole(Role.FACULTY_MEMBER);

        // Create authenticated admin user
        authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setFirstName("Admin");
        authenticatedUser.setLastName("User");
        authenticatedUser.setEmail("admin@example.com");
        authenticatedUser.setRole(Role.ADMIN);

        // Create test bookings
        booking1 = new Booking();
        booking1.setId(1L);
        booking1.setRoom(testRoom1);
        booking1.setUser(testUser1);
        booking1.setStartTime(LocalDateTime.of(2025, 8, 25, 10, 0));
        booking1.setEndTime(LocalDateTime.of(2025, 8, 25, 12, 0));
        booking1.setPurpose("Team meeting");
        booking1.setStatus(BookingStatus.PENDING);
        booking1.setCreatedAt(LocalDateTime.now());
        booking1.setUpdatedAt(LocalDateTime.now());

        booking2 = new Booking();
        booking2.setId(2L);
        booking2.setRoom(testRoom2);
        booking2.setUser(testUser2);
        booking2.setStartTime(LocalDateTime.of(2025, 8, 26, 14, 0));
        booking2.setEndTime(LocalDateTime.of(2025, 8, 26, 16, 0));
        booking2.setPurpose("Faculty presentation");
        booking2.setStatus(BookingStatus.APPROVED);
        booking2.setCreatedAt(LocalDateTime.now());
        booking2.setUpdatedAt(LocalDateTime.now());

        // Create booking request
        bookingRequest = new BookingRequest();
        bookingRequest.setUserId(101L);
        bookingRequest.setRoomId(201L);
        bookingRequest.setStartTime(LocalDateTime.now().plusDays(1).plusHours(2));
        bookingRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(4));
        bookingRequest.setPurpose("Team meeting");
    }

    @AfterEach
    void tearDown() {
        // Clean up the static mock after each test
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
            mockedSecurityContextHolder = null;
        }
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
        Mockito.lenient().when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(authenticatedUser));
    }

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(booking1, booking2));

        List<BookingResponse> bookings = bookingService.getAllBookings();

        assertEquals(2, bookings.size());
        assertEquals("Team meeting", bookings.get(0).getPurpose());
        assertEquals("Faculty presentation", bookings.get(1).getPurpose());
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void testGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        BookingResponse booking = bookingService.getBookingById(1L);

        assertNotNull(booking);
        assertEquals(testUser1.getId(), booking.getUserId());
        assertEquals(testRoom1.getId(), booking.getRoomId());
        assertEquals("Team meeting", booking.getPurpose());
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookingByIdNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(999L));
        verify(bookingRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateBookingSuccess() {
        // Setup mocks
        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(testRoom1));
        when(holidayRepository.existsByDateAndActiveTrue(any())).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(Collections.emptyList());

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setUser(testUser1);
        savedBooking.setRoom(testRoom1);
        savedBooking.setStartTime(bookingRequest.getStartTime());
        savedBooking.setEndTime(bookingRequest.getEndTime());
        savedBooking.setPurpose(bookingRequest.getPurpose());
        savedBooking.setStatus(BookingStatus.PENDING);
        savedBooking.setCreatedAt(LocalDateTime.now());
        savedBooking.setUpdatedAt(LocalDateTime.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Team meeting", response.getPurpose());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void testCreateBookingUserNotFound() {
        when(userRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(bookingRequest));
        verify(userRepository, times(1)).findById(101L);
    }

    @Test
    void testCreateBookingRoomNotFound() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(bookingRequest));
        verify(roomRepository, times(1)).findById(201L);
    }

    @Test
    void testCreateBookingOnHoliday() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(testRoom1));
        when(holidayRepository.existsByDateAndActiveTrue(any())).thenReturn(true);

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(bookingRequest));
        verify(holidayRepository, times(1)).existsByDateAndActiveTrue(any());
    }

    @Test
    void testCreateBookingEndTimeBeforeStartTime() {
        bookingRequest.setEndTime(bookingRequest.getStartTime().minusHours(1));

        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(testRoom1));
        when(holidayRepository.existsByDateAndActiveTrue(any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testCreateBookingOverlappingBookings() {
        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(testRoom1));
        when(holidayRepository.existsByDateAndActiveTrue(any())).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(Arrays.asList(booking1));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(bookingRequest));
        verify(bookingRepository, times(1)).findOverlappingBookings(any(), any(), any());
    }

    @Test
    void testCreateBookingInPast() {
        bookingRequest.setStartTime(LocalDateTime.now().minusHours(1));
        bookingRequest.setEndTime(LocalDateTime.now().plusHours(1));

        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));
        when(roomRepository.findById(201L)).thenReturn(Optional.of(testRoom1));
        when(holidayRepository.existsByDateAndActiveTrue(any())).thenReturn(false);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(Collections.emptyList());

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testCancelBookingSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        when(userRepository.findById(101L)).thenReturn(Optional.of(testUser1));

        Booking cancelledBooking = new Booking();
        cancelledBooking.setId(1L);
        cancelledBooking.setUser(testUser1);
        cancelledBooking.setRoom(testRoom1);
        cancelledBooking.setStartTime(booking1.getStartTime());
        cancelledBooking.setEndTime(booking1.getEndTime());
        cancelledBooking.setPurpose(booking1.getPurpose());
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        cancelledBooking.setRejectionReason("User cancelled");
        cancelledBooking.setCreatedAt(LocalDateTime.now());
        cancelledBooking.setUpdatedAt(LocalDateTime.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);

        BookingResponse response = bookingService.cancelBooking(1L, 101L, "User cancelled");

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals("User cancelled", response.getRejectionReason());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void testCancelBookingUnauthorized() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        when(userRepository.findById(102L)).thenReturn(Optional.of(testUser2)); // Different user

        assertThrows(SecurityException.class, () -> bookingService.cancelBooking(1L, 102L, "Cancel reason"));
    }

    @Test
    void testApproveBookingSuccess() {
        setupSecurityContext();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        Booking approvedBooking = new Booking();
        approvedBooking.setId(1L);
        approvedBooking.setUser(testUser1);
        approvedBooking.setRoom(testRoom1);
        approvedBooking.setStartTime(booking1.getStartTime());
        approvedBooking.setEndTime(booking1.getEndTime());
        approvedBooking.setPurpose(booking1.getPurpose());
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking.setCreatedAt(LocalDateTime.now());
        approvedBooking.setUpdatedAt(LocalDateTime.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        BookingResponse response = bookingService.approveBooking(1L);

        assertEquals(BookingStatus.APPROVED, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void testApproveBookingNonAdmin() {
        // Setup security context with non-admin user
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("student@example.com");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(testUser1));

        assertThrows(IllegalStateException.class, () -> bookingService.approveBooking(1L));
    }

    @Test
    void testRejectBookingSuccess() {
        setupSecurityContext();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        Booking rejectedBooking = new Booking();
        rejectedBooking.setId(1L);
        rejectedBooking.setUser(testUser1);
        rejectedBooking.setRoom(testRoom1);
        rejectedBooking.setStartTime(booking1.getStartTime());
        rejectedBooking.setEndTime(booking1.getEndTime());
        rejectedBooking.setPurpose(booking1.getPurpose());
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        rejectedBooking.setRejectionReason("Room not available");
        rejectedBooking.setCreatedAt(LocalDateTime.now());
        rejectedBooking.setUpdatedAt(LocalDateTime.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(rejectedBooking);

        BookingResponse response = bookingService.rejectBooking(1L, "Room not available");

        assertEquals(BookingStatus.REJECTED, response.getStatus());
        assertEquals("Room not available", response.getRejectionReason());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void testRejectBookingNonPending() {
        setupSecurityContext();

        booking1.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        assertThrows(IllegalStateException.class, () -> bookingService.rejectBooking(1L, "Reason"));
    }

    @Test
    void testApproveBookingNonPending() {
        setupSecurityContext();

        booking1.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        assertThrows(IllegalStateException.class, () -> bookingService.approveBooking(1L));
    }
}