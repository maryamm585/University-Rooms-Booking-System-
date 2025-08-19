package com.university.booking.dto.response;

import com.university.booking.entity.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;

    private Long roomId;

    private Long userId;

    private LocalDateTime startTime, endTime, createdAt, updatedAt;

    private String purpose;

    private BookingStatus status;

    private String rejectionReason;
}
