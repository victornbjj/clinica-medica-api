package com.clinic_api.service.impl;

import com.clinic_api.domain.User;
import com.clinic_api.service.UserService;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public User findById(UUID id) {
        return null;
    }

    @Override
    public void save(User medico) {

    }

    @Override
    public void update(User medico) {

    }

    @Override
    public void delete(UUID id) {

    }
}
