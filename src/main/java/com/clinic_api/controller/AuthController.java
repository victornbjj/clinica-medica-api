package com.clinic_api.controller;


import com.clinic_api.dto.AdminRegisterRequest;
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


    // QUALQUER USUARRIO PODE SE REGISTRAR, MAS APENAS USUARIOS COM ROLE ADMIN PODEM REGISTRAR OUTROS USUARIOS
    @PostMapping ("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    // APENAS USUARIOS COM ROLE ADMIN PODEM REGISTRAR COM ROLE DE MEDICO
    @PostMapping("admin/register")
    public ResponseEntity<Void> AdminRegister(@RequestBody AdminRegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResponseEntity.status(201).build();
    }

    // TODOS QUE PODEM SE REGISTRAR MAS INICIAL FICA COM ROLE CLIENTE
    @PostMapping("/register")
    public ResponseEntity<Void> registerPublic(@RequestBody RegisterRequest registerRequest){
        authService.registerPublic(registerRequest);
        return ResponseEntity.status(201).build();
    }

}
