package com.university.booking.service;

import com.university.booking.dto.request.BookingRequest;
import com.university.booking.dto.response.BookingResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingService {
    public BookingResponse createBooking(BookingRequest request) ;
    public List<BookingResponse> getAllBookings();
    public BookingResponse getBookingById(Long id);
    public BookingResponse cancelBooking(Long bookingId, Long userId, String reason);
}
