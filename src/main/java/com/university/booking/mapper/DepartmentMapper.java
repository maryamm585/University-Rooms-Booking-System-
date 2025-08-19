package com.university.booking.mapper;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.entity.Department;

public class DepartmentMapper {
    public static DepartmentDTO toDTO(Department dept) {
        return new DepartmentDTO(
                dept.getId(),
                dept.getName(),
                dept.getCode(),
                dept.getDescription(),
                dept.getActive()
        );
    }

    public static Department toEntity(DepartmentDTO dto) {
        Department dept = new Department();
        dept.setId(dto.getId());
        dept.setName(dto.getName());
        dept.setCode(dto.getCode());
        dept.setDescription(dto.getDescription());
        dept.setActive(dto.getActive());
        return dept;
    }
}
