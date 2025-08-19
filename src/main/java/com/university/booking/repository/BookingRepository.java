package com.university.booking.repository;

import com.university.booking.entity.Booking;
import com.university.booking.entity.Room;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.room = :room " +
            "AND b.status = 'APPROVED' " + // only consider approved bookings
            "AND ( (b.startTime < :endTime) AND (b.endTime > :startTime) )")
    List<Booking> findOverlappingBookings(
            @Param("room") Room room,
            @Future @Param("startTime") LocalDateTime startTime,
            @Future @Param("endTime") LocalDateTime endTime
    );
}
