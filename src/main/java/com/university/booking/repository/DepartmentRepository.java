package com.university.booking.repository;
import com.university.booking.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository  extends JpaRepository<Department, Long>{
    List<Department> findByActiveTrue();
    Optional<Department> findByCode(String code);
}
