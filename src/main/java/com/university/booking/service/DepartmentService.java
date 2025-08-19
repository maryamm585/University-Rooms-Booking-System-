package com.university.booking.service;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.entity.Department;
import com.university.booking.mapper.DepartmentMapper;
import com.university.booking.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepo;

    public DepartmentService(DepartmentRepository departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        Department dept = DepartmentMapper.toEntity(dto);
        return DepartmentMapper.toDTO(departmentRepo.save(dept));
    }

    public DepartmentDTO getDepartmentById(Long id) {
        return DepartmentMapper.toDTO(getDepartmentEntityById(id));
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepo.findAll()
                .stream()
                .map(DepartmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepo.findByActiveTrue()
                .stream()
                .map(DepartmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department existing = getDepartmentEntityById(id);
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setActive(dto.getActive());
        return DepartmentMapper.toDTO(departmentRepo.save(existing));
    }

    public DepartmentDTO activateDepartment(Long id) {
        Department dept = getDepartmentEntityById(id);
        dept.setActive(true);
        return DepartmentMapper.toDTO(departmentRepo.save(dept));
    }

    public DepartmentDTO deactivateDepartment(Long id) {
        Department dept = getDepartmentEntityById(id);
        dept.setActive(false);
        return DepartmentMapper.toDTO(departmentRepo.save(dept));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepo.existsById(id)) {
            throw new EntityNotFoundException("No department with id: " + id);
        }
        departmentRepo.deleteById(id);
    }

    private Department getDepartmentEntityById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No department with id: " + id));
    }
}
