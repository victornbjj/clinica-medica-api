package com.clinic_api.service.impl;

import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public List<User> findAll() {
        Role role = getCurrentUserRole();
        if(role != Role.ADMIN){
            throw new AccessDeniedException("Acesso negado");
        }
        return userRepository.findAll();
    }

    @Override
    public User findById(UUID id) {
        Role role = getCurrentUserRole();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if(role == Role.ADMIN ){

            return user;
        }

        if(role == Role.MEDICO ||role == Role.CLIENTE ){
            if(!getCurrentUser().getId().equals(id)){
                throw new AccessDeniedException("Acesso negado");
            }
            return user;
        }



        throw new AccessDeniedException("Acesso negado");

    }

    @Override
    public void save(User user) {


        boolean emailJaExiste =userRepository.findByEmail(user.getEmail()).isPresent();

        if(emailJaExiste){
            throw new BusinessException("Email já cadastrado");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));


        userRepository.save(user);


    }

    @Override
    public void update(User user) {
        Role role = getCurrentUserRole();

        if(user.getId() == null){
            throw new BusinessException("ID do usuário é obrigatório para atualização");
        }


        User userExiste = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if(role == Role.MEDICO || role == Role.CLIENTE ){
            User userLogado = getCurrentUser();

            if(!userLogado.getId().equals(userExiste.getId())){
                throw new AccessDeniedException("Acesso negado");
            }
            userRepository.save(user);
            return;
        }

        if(role == Role.ADMIN){
            userRepository.save(user);
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

        User existe = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        userRepository.delete(existe);

    }
}
