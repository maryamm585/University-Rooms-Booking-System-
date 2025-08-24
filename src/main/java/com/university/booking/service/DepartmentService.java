package com.university.booking.service;

import com.university.booking.dto.DepartmentDTO;
import com.university.booking.entity.Department;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.mapper.DepartmentMapper;
import com.university.booking.repository.DepartmentRepository;
import com.university.booking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepo;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        Department dept = DepartmentMapper.toEntity(dto);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A New Department with id {} was Created at {} by user with id {} and role {}",
                dept.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

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

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Department with id {} was updated at {} by user with id {} and role {}",
                existing.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
        return DepartmentMapper.toDTO(departmentRepo.save(existing));
    }

    public DepartmentDTO activateDepartment(Long id) {
        Department dept = getDepartmentEntityById(id);
        dept.setActive(true);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A Department with id {} was Activated at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
        return DepartmentMapper.toDTO(departmentRepo.save(dept));
    }

    public DepartmentDTO deactivateDepartment(Long id) {
        Department dept = getDepartmentEntityById(id);
        dept.setActive(false);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        logger.info("A Department with id {} was Deactivated at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
        return DepartmentMapper.toDTO(departmentRepo.save(dept));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepo.existsById(id)) {
            throw new EntityNotFoundException("No department with id: " + id);
        }

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        departmentRepo.deleteById(id);
        logger.info("A Department with id {} was deleted at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
    }

    private Department getDepartmentEntityById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No department with id: " + id));
    }
}
