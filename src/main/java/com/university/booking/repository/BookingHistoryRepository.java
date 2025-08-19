package com.university.booking.repository;

import com.university.booking.entity.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory,Long> {
}
