package com.university.booking.controller;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import com.university.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/all bookings")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        return bookingService.cancelBooking(id, userId, reason);
    }
}
