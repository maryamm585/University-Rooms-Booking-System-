package com.university.booking.repository;

import com.university.booking.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday,Long> {
    public Boolean existsByDateAndActiveTrue(LocalDate bookingDate);
}
