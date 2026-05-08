package com.clinic_api.service.impl;

import com.clinic_api.domain.Consulta;
import com.clinic_api.domain.Medico;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.enums.Role;
import com.clinic_api.enums.StatusConsulta;
import com.clinic_api.exception.AccessDeniedException;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.exception.ResourceNotFoundException;
import com.clinic_api.repository.ConsultaRepository;
import com.clinic_api.repository.MedicoRepository;
import com.clinic_api.repository.PacienteRepository;
import com.clinic_api.repository.UserRepository;
import com.clinic_api.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultaServiceImpl implements ConsultaService {



    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

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
    public List<Consulta> findAllByMedico(UUID idMedico) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN) {
            return consultaRepository.findAllByMedicoId(idMedico);
        }

        else if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));

            if (!medico.getId().equals(idMedico)) {
                throw new AccessDeniedException("Acesso negado");
            }
            return consultaRepository.findAllByMedicoId(idMedico);
        }
        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public List<Consulta> findAllByPaciente(UUID idPaciente) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN) {
            return consultaRepository.findAllByPacienteId(idPaciente);

        }
        if (role == Role.MEDICO) {
            return consultaRepository.findAllByPacienteId(idPaciente);
        }

        if (role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            if(!paciente.getId().equals(idPaciente)){
                throw new AccessDeniedException(("Acesso negado"));
            }
            return consultaRepository.findAllByPacienteId(idPaciente);
        }

        throw new AccessDeniedException(("Acesso negado"));
    }

    @Override
    public List<Consulta> findAll() {
        Role role = getCurrentUserRole();
        return switch (role) {
            case ADMIN -> consultaRepository.findAll();

            case MEDICO -> {
                Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
                yield  consultaRepository.findAllByMedicoId(medico.getId());
            }
            case CLIENTE -> {
                Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
                yield consultaRepository.findAllByPacienteId(paciente.getId());
            }
        };
    }

    @Override
    public Consulta findById(UUID id) {
        Role role = getCurrentUserRole();
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

        if(role == Role.ADMIN){
            return consulta;
        }
        if(role == Role.MEDICO){
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));
            if(!consulta.getMedico().getId().equals(medico.getId())){
                throw new AccessDeniedException(("Acesso negado"));
            }
            return consulta;
        }
        if(role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if(!consulta.getPaciente().getId().equals(paciente.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode ver suas proprias consultas"));
            }
            return consulta;
        }

        throw new AccessDeniedException("Acesso negado");

    }

    @Override
    public void save(Consulta consulta) {
        Role role = getCurrentUserRole();

        if(role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            if(!consulta.getPaciente().getId().equals(paciente.getId())){
                throw new AccessDeniedException("Acesso negado, voce só pode agendar consultas para voce mesmo");
            }
        }

        if(role == Role.MEDICO){
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));

            if(!consulta.getMedico().getId().equals(medico.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode agendar consultas para voce mesmo"));
            }
        }

        // ADMIN não tem restrição de identidade, apenas de conflito de horário

        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHora(consulta.getMedico().getId(), consulta.getDataHora());

        if(medicoOcupado){
            throw new BusinessException("Medico já possui consulta nesse horário");
        }
        boolean pacienteOcupado = consultaRepository.existsByPacienteIdAndDataHora(consulta.getPaciente().getId(), consulta.getDataHora());
        if(pacienteOcupado){
            throw  new BusinessException("Paciente já possui agendamento nesse horario");
        }
            consultaRepository.save(consulta);
    }

    @Override
    public void update(Consulta consulta) {
        Role role = getCurrentUserRole();

        Consulta existing = consultaRepository.findById(consulta.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Consulta não existente"));

        if(role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            if(!existing.getPaciente().getId().equals(paciente.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode alterar as proprias consultas"));
            }
        }

        if(role == Role.MEDICO){
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));

            if(!existing.getMedico().getId().equals(medico.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode alterar as proprias consultas"));
            }
        }

        // ADMIN não tem restrição de identidade, apenas de conflito de horário

        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndIdNot(consulta.getMedico().getId(),
                consulta.getDataHora(),
                consulta.getId());

        if(medicoOcupado){
            throw new BusinessException("Medico já possui consulta nesse horário");
        }

        boolean pacienteOcupado = consultaRepository.existsByPacienteIdAndDataHoraAndIdNot(consulta.getPaciente().getId(),
                consulta.getDataHora(),
                consulta.getId());

        if(pacienteOcupado){
            throw  new BusinessException("Paciente já possui agendamento nesse horario");
        }

        consultaRepository.save(consulta);
    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();

        Consulta existing = consultaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Consulta não existente"));

        if(role == Role.CLIENTE){
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

            if(!existing.getPaciente().getId().equals(paciente.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode alterar as proprias consultas"));
            }
        }

        if(role == Role.MEDICO){
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));

            if(!existing.getMedico().getId().equals(medico.getId())){
                throw new AccessDeniedException(("Acesso negado, voce só pode alterar as proprias consultas"));
            }
        }
        existing.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(existing);


    }
}
