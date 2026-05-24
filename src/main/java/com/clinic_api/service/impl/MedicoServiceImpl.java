package com.clinic_api.service.impl;

import com.clinic_api.domain.Medico;
import com.clinic_api.domain.User;
import com.clinic_api.dto.medico.MedicoResponse;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicoServiceImpl implements MedicoService {

     @Autowired
     private MedicoRepository medicoRepository;

     @Autowired
     private UserRepository userRepository;


    private String getCurrentUserEmail(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return (String) auth.getPrincipal();
    }


    private Role getCurrentUserRole(){
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

    private List<Medico> findVisibleMedicos() {
        Role role = getCurrentUserRole();
        if (role == Role.ADMIN || role == Role.CLIENTE) {
            return medicoRepository.findAll();
        }
        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado para o usuário"));
            return List.of(medico);
        }
        throw new AccessDeniedException("Acesso negado");
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

    @Override
    public List<MedicoResponse> findAll() {
        Role role = getCurrentUserRole();
        return findVisibleMedicos().stream()
                .map(role == Role.ADMIN ? this::toAdminResponse : this::toPublicResponse)
                .toList();
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
    public void save(Medico medico) {
        Role role = getCurrentUserRole();


        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado: apenas ADMIN pode cadastrar médicos");
        }


        boolean crmJaExiste = medicoRepository.findByCrm(medico.getCrm()).isPresent();
        if (crmJaExiste) {
            throw new BusinessException("Já existe um médico cadastrado com esse CRM");
        }


        boolean userJaVinculado = medicoRepository.findByIdUserId(medico.getIdUser().getId()).isPresent();
        if (userJaVinculado) {
            throw new BusinessException("Esse usuário já possui um médico cadastrado");
        }

        medicoRepository.save(medico);


    }

    @Override
    public void update(Medico medico) {
        Role role = getCurrentUserRole();

        if(role == Role.CLIENTE){
            throw new AccessDeniedException("Acesso negado");
        }

        if(medico.getId() == null){
            throw new BusinessException("ID do médico é obrigatório para atualização");
        }

        Medico existe = medicoRepository.findById(medico.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));

        if(role == Role.ADMIN){
            medicoRepository.save(medico);
            return;
        }

        if(role == Role.MEDICO){
            Medico medicoLogado = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado para o usuário"));

            if(!medicoLogado.getId().equals(existe.getId())){
                throw new AccessDeniedException("Acesso negado");
            }
            medicoRepository.save(medico);
            return;
        }
        throw new AccessDeniedException("Acesso negado");

    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();


        if(role != Role.ADMIN){
            throw new AccessDeniedException("Acesso negado");
        }

        if(id == null){
            throw new BusinessException("ID do médico é obrigatório para atualização");
        }

        Medico existe = medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));

        medicoRepository.delete(existe);

    }
}
