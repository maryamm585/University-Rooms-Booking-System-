package com.university.booking.service.impl;

import com.university.booking.dto.request.UserCreateRequest;
import com.university.booking.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserServiceImplementation extends UserDetailsService {

    UserResponse createUser(UserCreateRequest createRequest);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);

}