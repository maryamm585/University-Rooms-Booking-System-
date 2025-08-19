package com.university.booking.controller;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/ADMIN/createdepartment")
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.createDepartment(dto));
    }

    @GetMapping("/All/getdepartmentbyid/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/ADMIN/getalldepartments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/All/activedepartments")
    public ResponseEntity<List<DepartmentDTO>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @PutMapping("/ADMIN/updatedepartment/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @PutMapping("/ADMIN/activatedep/{id}")
    public ResponseEntity<DepartmentDTO> activateDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.activateDepartment(id));
    }

    @PutMapping("/ADMIN/deactivatedep/{id}")
    public ResponseEntity<DepartmentDTO> deactivateDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.deactivateDepartment(id));
    }

    @DeleteMapping("/ADMIN/deletedep/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
