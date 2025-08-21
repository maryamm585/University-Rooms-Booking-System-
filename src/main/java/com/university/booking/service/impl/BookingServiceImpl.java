
package com.university.booking.service.impl;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import com.university.booking.entity.*;
import com.university.booking.exception.BookingConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.*;
import com.university.booking.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final HolidayRepository holidayRepository;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id:" + request.getUserId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id:" + request.getRoomId()));

        LocalDate bookingDate = request.getStartTime().toLocalDate();

        if(holidayRepository.existsByDateAndActiveTrue(bookingDate)){
            throw new BookingConflictException("Bookings are not allowed on holidays");
        }

        if(request.getEndTime().isBefore(request.getStartTime())){
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<Booking> duplicates = bookingRepository.findOverlappingBookings(
                room, request.getStartTime(), request.getEndTime()
        );
        if(!duplicates.isEmpty()){
            throw new BookingConflictException("Room is already booked for the selected time");
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
