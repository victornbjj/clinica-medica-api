package com.clinic_api.service.impl;

import com.clinic_api.domain.Endereco;
import com.clinic_api.domain.Medico;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.dto.AdminRegisterRequest;
import com.clinic_api.enums.Role;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.EnderecoRepository;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.repository.security.JwtUtil;
import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.LoginResponse;
import com.clinic_api.dto.RegisterRequest;
import com.clinic_api.service.AuthService;
import com.clinic_api.service.ViaCepService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl  implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private ViaCepService viaCepService;


    @Autowired
    PasswordEncoder passwordEncoder;


    @Override
    public LoginResponse login(@NonNull LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));


        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AccessDeniedException("Senha incorreta");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());


        return new LoginResponse(token);
    }

    @Override
    public void register(AdminRegisterRequest request) {

        if(request.getRole() == null){
            throw new BusinessException("O campo 'role' é obrigatório");
        }


        boolean emailJaExiste = userRepository.findByEmail(request.getEmail()).isPresent();
        if (emailJaExiste) {
            throw new BusinessException("Email já cadastrado");
        }


        Endereco endereco = viaCepService.consultarCep(request.getCep());
        endereco.setNumero(request.getNumero());
        enderecoRepository.save(endereco);


        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEndereco(endereco);
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

    @Override
    public void registerPublic(RegisterRequest request) {


        boolean emailJaExiste = userRepository.findByEmail(request.getEmail()).isPresent();
        if (emailJaExiste) {
            throw new BusinessException("Email já cadastrado");
        }

        Endereco endereco = viaCepService.consultarCep(request.getCep());
        endereco.setNumero(request.getNumero());
        enderecoRepository.save(endereco);


        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENTE);
        user.setEndereco(endereco);
        userRepository.save(user);


        Paciente paciente = new Paciente();
        paciente.setName(request.getNome());
        paciente.setIdUser(user);
        pacienteRepository.save(paciente);

    }
}
