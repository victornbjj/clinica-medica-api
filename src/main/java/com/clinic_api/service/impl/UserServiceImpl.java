package com.clinic_api.service.impl;

import com.clinic_api.domain.User;
import com.clinic_api.dto.user.UserResponse;
import com.clinic_api.dto.user.UserUpdateRequest;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return (String) auth.getPrincipal();
    }

    private Role getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(a -> Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Role não encontrada"));
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Override
    public Page<UserResponse> findAll(Pageable pageable) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Override
    public UserResponse findById(UUID id) {
        Role role = getCurrentUserRole();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (role == Role.ADMIN) {
            return UserResponse.from(user);
        }

        if (role == Role.MEDICO || role == Role.CLIENTE) {
            if (!getCurrentUser().getId().equals(id)) {
                throw new AccessDeniedException("Acesso negado");
            }
            return UserResponse.from(user);
        }

        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public UserResponse findMe() {
        return UserResponse.from(getCurrentUser());
    }

    @Override
    public void update(UUID id, UserUpdateRequest request) {
        Role role = getCurrentUserRole();

        User userExiste = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (role == Role.MEDICO || role == Role.CLIENTE) {
            if (!getCurrentUser().getId().equals(userExiste.getId())) {
                throw new AccessDeniedException("Acesso negado");
            }
            applyUpdate(userExiste, request, false);
            userRepository.save(userExiste);
            return;
        }

        if (role == Role.ADMIN) {
            applyUpdate(userExiste, request, true);
            userRepository.save(userExiste);
            return;
        }

        throw new AccessDeniedException("Acesso negado");
    }

    private void applyUpdate(User user, UserUpdateRequest request, boolean allowRoleChange) {
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email já cadastrado");
        }

        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (allowRoleChange && request.getRole() != null) {
            user.setRole(request.getRole());
        }
    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }

        User existe = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        userRepository.delete(existe);
    }
}
