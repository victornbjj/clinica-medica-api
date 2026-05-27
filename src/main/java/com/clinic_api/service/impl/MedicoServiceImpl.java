package com.clinic_api.service.impl;

import com.clinic_api.domain.Medico;
import com.clinic_api.domain.User;
import com.clinic_api.dto.medico.MedicoRequest;
import com.clinic_api.dto.medico.MedicoResponse;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.MedicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final UserRepository userRepository;

    public MedicoServiceImpl(MedicoRepository medicoRepository, UserRepository userRepository) {
        this.medicoRepository = medicoRepository;
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

    private MedicoResponse toPublicResponse(Medico medico) {
        return new MedicoResponse(
                medico.getId(),
                medico.getNome(),
                medico.getEspecialidade(),
                null,
                null
        );
    }

    private MedicoResponse toAdminResponse(Medico medico) {
        return new MedicoResponse(
                medico.getId(),
                medico.getNome(),
                medico.getEspecialidade(),
                medico.getCrm(),
                medico.getIdUser().getEmail()
        );
    }

    private Page<MedicoResponse> mapPage(Page<Medico> page, boolean adminView) {
        return page.map(adminView ? this::toAdminResponse : this::toPublicResponse);
    }

    private boolean matchesEspecialidade(Medico medico, String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            return true;
        }
        return medico.getEspecialidade().toLowerCase().contains(especialidade.trim().toLowerCase());
    }

    @Override
    public Page<MedicoResponse> findAll(Pageable pageable, String especialidade) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN) {
            Page<Medico> page = hasEspecialidadeFilter(especialidade)
                    ? medicoRepository.findByEspecialidadeContainingIgnoreCase(especialidade.trim(), pageable)
                    : medicoRepository.findAll(pageable);
            return mapPage(page, true);
        }

        if (role == Role.CLIENTE) {
            Page<Medico> page = hasEspecialidadeFilter(especialidade)
                    ? medicoRepository.findByEspecialidadeContainingIgnoreCase(especialidade.trim(), pageable)
                    : medicoRepository.findAll(pageable);
            return mapPage(page, false);
        }

        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado para o usuário"));
            if (!matchesEspecialidade(medico, especialidade)) {
                return Page.empty(pageable);
            }
            return new PageImpl<>(List.of(toAdminResponse(medico)), pageable, 1);
        }

        throw new AccessDeniedException("Acesso negado");
    }

    private boolean hasEspecialidadeFilter(String especialidade) {
        return especialidade != null && !especialidade.isBlank();
    }

    @Override
    public MedicoResponse findById(UUID id) {
        Role role = getCurrentUserRole();
        if (role == Role.ADMIN) {
            Medico medico = medicoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
            return toAdminResponse(medico);
        }
        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado para o usuário"));
            if (!medico.getId().equals(id)) {
                throw new AccessDeniedException("Médico só pode acessar seus próprios dados");
            }
            return toAdminResponse(medico);
        }
        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public void save(MedicoRequest request) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado: apenas ADMIN pode cadastrar médicos");
        }

        if (medicoRepository.findByCrm(request.getCrm()).isPresent()) {
            throw new BusinessException("Já existe um médico cadastrado com esse CRM");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (medicoRepository.findByIdUserId(user.getId()).isPresent()) {
            throw new BusinessException("Esse usuário já possui um médico cadastrado");
        }

        Medico medico = new Medico();
        medico.setNome(request.getNome());
        medico.setCrm(request.getCrm());
        medico.setEspecialidade(request.getEspecialidade());
        medico.setIdUser(user);
        medicoRepository.save(medico);
    }

    @Override
    public void update(UUID id, MedicoRequest request) {
        Role role = getCurrentUserRole();
        if (role == Role.CLIENTE) {
            throw new AccessDeniedException("Acesso negado");
        }

        Medico existe = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));

        if (role == Role.MEDICO) {
            Medico medicoLogado = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado para o usuário"));
            if (!medicoLogado.getId().equals(existe.getId())) {
                throw new AccessDeniedException("Acesso negado");
            }
        }

        existe.setNome(request.getNome());
        existe.setCrm(request.getCrm());
        existe.setEspecialidade(request.getEspecialidade());

        if (role == Role.ADMIN && request.getUserId() != null
                && !request.getUserId().equals(existe.getIdUser().getId())) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            if (medicoRepository.findByIdUserId(user.getId()).isPresent()) {
                throw new BusinessException("Esse usuário já possui um médico cadastrado");
            }
            existe.setIdUser(user);
        }

        medicoRepository.save(existe);
    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }

        Medico existe = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
        medicoRepository.delete(existe);
    }
}
