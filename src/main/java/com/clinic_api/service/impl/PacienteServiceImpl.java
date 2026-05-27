package com.clinic_api.service.impl;

import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.dto.paciente.PacienteRequest;
import com.clinic_api.dto.paciente.PacienteResponse;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.PacienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepository;

    public PacienteServiceImpl(PacienteRepository pacienteRepository, UserRepository userRepository) {
        this.pacienteRepository = pacienteRepository;
        this.userRepository = userRepository;
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
    public Page<PacienteResponse> findAll(Pageable pageable) {
        Role role = getCurrentUserRole();
        if (role == Role.ADMIN || role == Role.MEDICO) {
            return pacienteRepository.findAll(pageable).map(PacienteResponse::from);
        }
        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            return new PageImpl<>(List.of(PacienteResponse.from(paciente)), pageable, 1);
        }
        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public PacienteResponse findById(UUID id) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN || role == Role.MEDICO) {
            Paciente paciente = pacienteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            return PacienteResponse.from(paciente);
        }
        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!paciente.getId().equals(id)) {
                throw new AccessDeniedException("Acesso negado");
            }
            return PacienteResponse.from(paciente);
        }

        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public void save(PacienteRequest request) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN && role != Role.MEDICO) {
            throw new AccessDeniedException("Acesso negado");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (pacienteRepository.findByIdUserId(user.getId()).isPresent()) {
            throw new BusinessException("Esse usuário já possui um paciente cadastrado");
        }

        Paciente paciente = new Paciente();
        paciente.setName(request.getName());
        paciente.setIdUser(user);
        pacienteRepository.save(paciente);
    }

    @Override
    public void update(UUID id, PacienteRequest request) {
        Role role = getCurrentUserRole();

        Paciente existe = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        if (role == Role.CLIENTE) {
            Paciente pacienteLogado = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!pacienteLogado.getId().equals(existe.getId())) {
                throw new AccessDeniedException("Acesso negado");
            }
            existe.setName(request.getName());
            pacienteRepository.save(existe);
            return;
        }

        if (role == Role.ADMIN || role == Role.MEDICO) {
            existe.setName(request.getName());
            if (request.getUserId() != null && !request.getUserId().equals(existe.getIdUser().getId())) {
                User user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                if (pacienteRepository.findByIdUserId(user.getId()).isPresent()) {
                    throw new BusinessException("Esse usuário já possui um paciente cadastrado");
                }
                existe.setIdUser(user);
            }
            pacienteRepository.save(existe);
            return;
        }

        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }

        Paciente existe = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        pacienteRepository.delete(existe);
    }
}
