package com.university.booking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull(message = "missed")
    private Long roomId;

    @NotNull(message = "missed")
    private Long userId;

    @NotNull(message = "missed")
    @Future(message = "time must be in future")
    private LocalDateTime startTime;

    @NotNull(message = "missed")
    private LocalDateTime endTime;

    @NotBlank(message = "missed")
    private String purpose;
}
