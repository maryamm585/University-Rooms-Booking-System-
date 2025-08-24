package com.university.booking.service;

import com.university.booking.dto.auth.AuthenticationRequest;
import com.university.booking.dto.auth.AuthenticationResponse;
import com.university.booking.dto.auth.RegisterRequest;
import com.university.booking.entity.Department;
import com.university.booking.repository.DepartmentRepository;
import com.university.booking.repository.UserRepository;
import com.university.booking.entity.User;
import com.university.booking.security.JwtService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthenticationResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());

        Department department = departmentRepository.findByCode(request.getDepartmentCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid department code"));
        user.setDepartment(department);
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        logger.info("new User with Id: {} and Role {} is Created At {}", user.getId(), user.getRole(), new Date(System.currentTimeMillis()));
        return new AuthenticationResponse()
                .builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("Invalid email"));
        String jwtToken = jwtService.generateToken(user);
        logger.info("User with Id: {} and Role {} is Logged Into The System at {}", user.getId(), user.getRole(), new Date(System.currentTimeMillis()));
        return new AuthenticationResponse()
                .builder()
                .token(jwtToken)
                .build();
    }
}
