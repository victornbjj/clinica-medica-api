package com.clinic_api.support;

import com.clinic_api.domain.Endereco;
import com.clinic_api.domain.Medico;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.enums.Role;
import com.clinic_api.repository.ConsultaRepository;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.repository.security.JwtUtil;
import com.clinic_api.service.CepService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MedicoRepository medicoRepository;

    @Autowired
    protected PacienteRepository pacienteRepository;

    @Autowired
    protected ConsultaRepository consultaRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtil jwtUtil;

    @MockBean
    protected CepService cepService;

    protected User admin;
    protected User cliente;
    protected User medicoUser;
    protected Medico medico;
    protected Paciente paciente;

    @BeforeEach
    void baseSetUp() {
        consultaRepository.deleteAll();
        medicoRepository.deleteAll();
        pacienteRepository.deleteAll();
        userRepository.deleteAll();

        when(cepService.consultarCep(anyString())).thenAnswer(invocation -> {
            Endereco endereco = new Endereco();
            endereco.setRua("Rua Mock");
            endereco.setCidade("São Paulo");
            endereco.setEstado("SP");
            endereco.setCep(invocation.getArgument(0));
            return endereco;
        });

        admin = createUser("admin@test.com", "admin123", Role.ADMIN);
        cliente = createUser("cliente@test.com", "senha123", Role.CLIENTE);
        medicoUser = createUser("medico@test.com", "senha123", Role.MEDICO);

        medico = new Medico();
        medico.setNome("Dr. Teste");
        medico.setCrm("CRM-001");
        medico.setEspecialidade("Cardiologia");
        medico.setIdUser(medicoUser);
        medico = medicoRepository.save(medico);

        paciente = new Paciente();
        paciente.setName("Paciente Teste");
        paciente.setIdUser(cliente);
        paciente = pacienteRepository.save(paciente);
    }

    protected User createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }

    protected String bearerToken(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}
