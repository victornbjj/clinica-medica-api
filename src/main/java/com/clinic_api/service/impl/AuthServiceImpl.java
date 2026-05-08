package com.clinic_api.service.impl;

import com.clinic_api.domain.Medico;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.repository.security.JwtUtil;
import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.LoginResponse;
import com.clinic_api.dto.RegisterRequest;
import com.clinic_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthServiceImpl  implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    PasswordEncoder passwordEncoder;


    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));


        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AccessDeniedException("Senha incorreta");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());


        return new LoginResponse(token);
    }

    @Override
    public void register(RegisterRequest request) {
        boolean emailJaExiste = userRepository.findByEmail(request.getEmail()).isPresent();
        if (emailJaExiste) {
            throw new BusinessException("Email já cadastrado");
        }


        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);


        if (request.getRole() == Role.MEDICO) {
            Medico medico = new Medico();
            medico.setNome(request.getNome());
            medico.setCrm(request.getCrm());
            medico.setEspecialidade(request.getEspecialidade());
            medico.setIdUser(user);
            medicoRepository.save(medico);
        }

        if (request.getRole() == Role.CLIENTE) {
            Paciente paciente = new Paciente();
            paciente.setName(request.getNome());
            paciente.setIdUser(user);
            pacienteRepository.save(paciente);
        }

    }
}
