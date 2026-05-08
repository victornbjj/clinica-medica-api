package com.clinic_api.service.impl;

import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;

import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
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


    @Override
    public List<Paciente> findAll() {
        Role role =  getCurrentUserRole();
        if(role == Role.ADMIN || role == Role.MEDICO){
            return pacienteRepository.findAll();
        }
        else if(role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            return List.of(paciente);
        }
        else{
            throw new AccessDeniedException("Acesso negado");
        }
    }

    @Override
    public Paciente findById(UUID id) {
        Role role = getCurrentUserRole();


        if(role == Role.ADMIN|| role == Role.MEDICO){
            return pacienteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        }
        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if(!paciente.getId().equals(id)){
                throw new AccessDeniedException("Acesso negado");
            }
            return paciente;
        }

        throw new AccessDeniedException("Acesso negado");

    }

    @Override
    public void save(Paciente paciente){
        Role role = getCurrentUserRole();

        if(role != Role.ADMIN && role != Role.MEDICO){
            throw new AccessDeniedException("Acesso negado");
        }

        boolean jaExiste = pacienteRepository.findByIdUserId(paciente.getIdUser().getId()).isPresent();
        if (jaExiste) {
            throw new BusinessException("Esse usuário já possui um paciente cadastrado");
        }
        pacienteRepository.save(paciente);
    }

    @Override
    public void update(Paciente paciente) {
        Role role = getCurrentUserRole();

        if(paciente.getId() == null){
            throw new BusinessException("ID do paciente é obrigatório para atualização");
        }

         Paciente existe = pacienteRepository.findById(paciente.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));


        if(role == Role.CLIENTE){
            Paciente pacienteLogado = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            if (!pacienteLogado.getId().equals(existe.getId())){
                throw new AccessDeniedException("Acesso negado");
            }
            pacienteRepository.save(paciente);
            return;
        }

        if(role == Role.ADMIN || role == Role.MEDICO){
            pacienteRepository.save(paciente);
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

        Paciente existe = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        pacienteRepository.delete(existe);


    }
}
