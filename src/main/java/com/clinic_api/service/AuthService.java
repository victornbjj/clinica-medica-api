package com.clinic_api.service;

import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.LoginResponse;
import com.clinic_api.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService  {

    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);



}
