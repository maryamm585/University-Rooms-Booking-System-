package com.university.booking.controller;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createdepartment")
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.createDepartment(dto));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/getdepartmentbyid/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getalldepartments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY_MEMBER')")
    @GetMapping("/activedepartments")
    public ResponseEntity<List<DepartmentDTO>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updatedepartment/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activatedep/{id}")
    public ResponseEntity<DepartmentDTO> activateDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.activateDepartment(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivatedep/{id}")
    public ResponseEntity<DepartmentDTO> deactivateDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.deactivateDepartment(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deletedep/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
