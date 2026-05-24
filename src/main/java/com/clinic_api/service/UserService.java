package com.clinic_api.service;

import com.clinic_api.domain.User;
import com.clinic_api.dto.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponse> findAll();

    UserResponse findById(UUID id);

    UserResponse findMe();

    void save(User user);

    void update(User user);

    void delete(UUID id);
}
