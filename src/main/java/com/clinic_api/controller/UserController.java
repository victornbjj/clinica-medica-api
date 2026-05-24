package com.clinic_api.controller;

import com.clinic_api.domain.User;
import com.clinic_api.dto.user.UserResponse;
import com.clinic_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users/me
     * Retorna o usuário autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.findMe());
    }

    /**
     * GET /users
     * Lista todos os usuários.
     * Segurança: apenas ADMIN (validação também no service).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * GET /users/{id}
     * Detalhes de um usuário.
     * Regras aplicadas pelo service:
     * - ADMIN: qualquer usuário
     * - MEDICO e CLIENTE: apenas o próprio usuário
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * PUT /users/{id}
     * Atualiza dados de um usuário.
     * Regras aplicadas pelo service:
     * - ADMIN: qualquer usuário
     * - MEDICO e CLIENTE: apenas o próprio usuário
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid User user) {
        user.setId(id);
        userService.update(user);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /users/{id}
     * Remove um usuário.
     * Segurança: apenas ADMIN pode deletar.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
