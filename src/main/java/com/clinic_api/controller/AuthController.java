package com.clinic_api.controller;


import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.LoginResponse;
import com.clinic_api.dto.RegisterRequest;
import com.clinic_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping ("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("admin/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResponseEntity.status(201).build();
    }

}
