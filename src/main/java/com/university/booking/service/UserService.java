package com.university.booking.service;

import com.university.booking.dto.request.UserCreateRequest;
import com.university.booking.dto.response.UserResponse;
import com.university.booking.entity.Role;
import com.university.booking.entity.User;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.repository.UserRepository;
import com.university.booking.service.impl.UserServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceImplementation {

    private final UserRepository userRepository;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(RoomFeatureService.class);



    @Override
    public UserResponse createUser(UserCreateRequest createRequest) {
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        Role role;
        try {
            role = Role.valueOf(createRequest.getRole().name().toUpperCase());
            // ✅ will throw IllegalArgumentException if not valid
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + createRequest.getRole().name());
        }

        User user = new User();
        user.setFirstName(createRequest.getFirstName());
        user.setLastName(createRequest.getLastName());
        user.setEmail(createRequest.getEmail());
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A New User with id {} was Created at {} by user with id {} and role {}",
                savedUser.getId(),
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        // get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        logger.info("A New User with id {} was Deleted at {} by user with id {} and role {}",
                id,
                new Date(System.currentTimeMillis()),
                currentUser.getId(),
                currentUser.getRole().name()
        );
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert your Role enum to GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(authority)   // <-- must be a collection
        );
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());

        if (user.getRole() != null) {

            response.setRole(user.getRole());
        }
        return response;
    }
}