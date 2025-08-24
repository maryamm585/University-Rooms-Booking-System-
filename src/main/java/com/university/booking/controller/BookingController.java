package com.university.booking.controller;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import com.university.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY_MEMBER')")
    @PostMapping("/create")
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY_MEMBER')")
    @PostMapping("/cancel/{id}")
    public BookingResponse cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        return bookingService.cancelBooking(id, userId, reason);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ResponseEntity<BookingResponse> approveBooking(@PathVariable Long id) {
        // call service to approve
        BookingResponse response = bookingService.approveBooking(id);

        // return 200 OK with updated booking
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{id}")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        BookingResponse response = bookingService.rejectBooking(id, reason);
        return ResponseEntity.ok(response);
    }


}
