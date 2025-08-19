package com.university.booking.repository;

import com.university.booking.entity.RoomFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomFeatureRepository extends JpaRepository<RoomFeature, Long> {

    // check uniqueness by name
    boolean existsByName(String name);
}
