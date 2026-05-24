package com.clinic_api.controller;


import com.clinic_api.dto.AdminRegisterRequest;
import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.LoginResponse;
import com.clinic_api.dto.RegisterRequest;
import com.clinic_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login
     * Autenticação pública — retorna JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * POST /auth/admin/register
     * Registro administrativo (médico, paciente vinculado, etc.).
     * Segurança: apenas ADMIN.
     */
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminRegister(@RequestBody @Valid AdminRegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * POST /auth/register
     * Registro público — role inicial CLIENTE.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerPublic(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.registerPublic(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
