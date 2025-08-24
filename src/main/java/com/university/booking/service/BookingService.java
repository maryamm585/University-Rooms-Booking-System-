
package com.university.booking.service;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import com.university.booking.entity.*;
import com.university.booking.exception.BookingConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.exception.UnauthorizedActionException;
import com.university.booking.repository.*;
import com.university.booking.service.BookingService;
import com.university.booking.service.impl.BookingServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final HolidayRepository holidayRepository;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id:" + request.getUserId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id:" + request.getRoomId()));

        LocalDate bookingDate = request.getStartTime().toLocalDate();

        // Check if Booking Was On Holidays
        if(holidayRepository.existsByDateAndActiveTrue(bookingDate)){
            throw new BookingConflictException("Bookings are not allowed on holidays");
        }

        // Check if Start is Before End Time
        if(request.getEndTime().isBefore(request.getStartTime())){
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check to See if there is a Duplicate Reservation
        List<Booking> duplicates = bookingRepository.findOverlappingBookings(
                room, request.getStartTime(), request.getEndTime()
        );

        if(!duplicates.isEmpty()){
            throw new BookingConflictException("Room is already booked for the selected time");
        }

        // Cannot book in the past
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingConflictException("Cannot book a room in the past");
        }

        // Users cannot book more than 90 days ahead of today
        if (request.getStartTime().isAfter(LocalDateTime.now().plusDays(90))) {
            throw new BookingConflictException("Bookings cannot be made more than 90 days in advance");
        }

        // Minimum booking notice (at least 1hr before)
        if (request.getStartTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Bookings must be made at least 1 hour in advance");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        BookingHistory history = new BookingHistory();
        history.setBooking(saved);
        history.setActor(user);
        history.setPreviousStatus(BookingStatus.CANCELLED);
        history.setNewStatus(BookingStatus.PENDING);
        history.setAction("CREATED");

        bookingHistoryRepository.save(history);

        logger.info(
                "Booking with id: {} created at {} by user with id: {} and Role: {}",
                booking.getId(),
                booking.getCreatedAt(),
                user.getId(),
                user.getRole()
        );
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(room.getId())
                .userId(user.getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .status(booking.getStatus())
                .rejectionReason(booking.getRejectionReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(booking -> BookingResponse.builder()
                        .id(booking.getId())
                        .roomId(booking.getRoom().getId())
                        .userId(booking.getUser().getId())
                        .startTime(booking.getStartTime())
                        .endTime(booking.getEndTime())
                        .purpose(booking.getPurpose())
                        .status(booking.getStatus())
                        .rejectionReason(booking.getRejectionReason())
                        .createdAt(booking.getCreatedAt())
                        .updatedAt(booking.getUpdatedAt())
                        .build()).toList();
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .userId(booking.getUser().getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .status(booking.getStatus())
                .rejectionReason(booking.getRejectionReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!(booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.APPROVED)) {
            throw new IllegalStateException(
                    "Only PENDING or APPROVED bookings can be cancelled. Current status: " + booking.getStatus()
            );
        }

        boolean isAdmin = user.getRole().equals(Role.ADMIN);

        if (!isAdmin) {
            // requester can only cancel own booking
            if (!booking.getUser().getId().equals(userId)) {
                throw new SecurityException("You can only cancel your own bookings");
            }

            // requester can only cancel before start time
            if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("You cannot cancel a booking that has already started");
            }
        }


        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRejectionReason(reason);

        Booking saved = bookingRepository.save(booking);

        BookingHistory history = new BookingHistory();
        history.setBooking(saved);
        history.setActor(user);
        history.setPreviousStatus(BookingStatus.PENDING);
        history.setNewStatus(BookingStatus.CANCELLED);
        history.setAction("CANCELLED");
        history.setReason(reason);

        bookingHistoryRepository.save(history);

        logger.info(
                "Booking with id: {} is cancelled at {} by user with id: {} and Role: {}",
                booking.getId(),
                new Date(System.currentTimeMillis()),
                user.getId(),
                user.getRole()
        );

        return BookingResponse.builder()
                .id(saved.getId())
                .roomId(saved.getRoom().getId())
                .userId(saved.getUser().getId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .purpose(saved.getPurpose())
                .status(saved.getStatus())
                .rejectionReason(saved.getRejectionReason())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public BookingResponse approveBooking(Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only ADMIN users can approve booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be approved");
        }

        // 5️⃣ Approve booking
        booking.setStatus(BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);

        // 6️⃣ Log history
        BookingHistory history = new BookingHistory();
        history.setBooking(saved);
        history.setActor(currentUser);
        history.setPreviousStatus(BookingStatus.PENDING);
        history.setNewStatus(BookingStatus.APPROVED);
        history.setAction("APPROVED");
        bookingHistoryRepository.save(history);

        logger.info(
                "Booking with id: {} is Approved at {} by user with id: {} and Role: {}",
                booking.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole()
        );

        // 7️⃣ Map to response DTO
        return BookingResponse.builder()
                .id(saved.getId())
                .roomId(saved.getRoom().getId())
                .userId(saved.getUser().getId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .purpose(saved.getPurpose())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public BookingResponse rejectBooking(Long bookingId, String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admins can reject bookings");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);  // store reason
        Booking saved = bookingRepository.save(booking);

        BookingHistory history = new BookingHistory();
        history.setBooking(saved);
        history.setActor(currentUser);
        history.setPreviousStatus(BookingStatus.PENDING);
        history.setNewStatus(BookingStatus.REJECTED);
        history.setAction("REJECTED");
        history.setReason(reason);
        bookingHistoryRepository.save(history);

        logger.info(
                "Booking with id: {} is Rejected at {} by user with id: {} and Role: {}",
                booking.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole()
        );

        return BookingResponse.builder()
                .id(saved.getId())
                .roomId(saved.getRoom().getId())
                .userId(saved.getUser().getId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .purpose(saved.getPurpose())
                .status(saved.getStatus())
                .rejectionReason(saved.getRejectionReason())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }


}
