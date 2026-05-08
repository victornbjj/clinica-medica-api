package com.clinic_api.service;

import com.clinic_api.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<User> findAll();


    User findById(UUID id);

    void save(User user);

    void update(User user);

    void delete(UUID id);
}
