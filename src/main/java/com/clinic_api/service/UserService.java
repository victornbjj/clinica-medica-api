package com.clinic_api.service;

import com.clinic_api.dto.user.UserResponse;
import com.clinic_api.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> findAll(Pageable pageable);

    UserResponse findById(UUID id);

    UserResponse findMe();

    void update(UUID id, UserUpdateRequest request);

    void delete(UUID id);
}
