package com.clinic_api.service.impl;

import com.clinic_api.domain.Consulta;
import com.clinic_api.domain.Medico;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.dto.consulta.ConsultaRequest;
import com.clinic_api.dto.consulta.ConsultaResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultaServiceImpl implements ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepository;

    public ConsultaServiceImpl(
            ConsultaRepository consultaRepository,
            MedicoRepository medicoRepository,
            PacienteRepository pacienteRepository,
            UserRepository userRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
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

    private List<ConsultaResponse> toResponseList(List<Consulta> consultas) {
        return consultas.stream().map(ConsultaResponse::from).toList();
    }

    private Consulta buildConsulta(ConsultaRequest request) {
        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Consulta consulta = new Consulta();
        consulta.setDataHora(request.getDataHora());
        consulta.setStatus(request.getStatus());
        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        return consulta;
    }

    private void validateScheduleConflict(Consulta consulta, UUID consultaIdToIgnore) {
        UUID medicoId = consulta.getMedico().getId();
        UUID pacienteId = consulta.getPaciente().getId();

        boolean medicoOcupado = consultaIdToIgnore == null
                ? consultaRepository.existsByMedicoIdAndDataHora(medicoId, consulta.getDataHora())
                : consultaRepository.existsByMedicoIdAndDataHoraAndIdNot(medicoId, consulta.getDataHora(), consultaIdToIgnore);

        if (medicoOcupado) {
            throw new BusinessException("Medico já possui consulta nesse horário");
        }

        boolean pacienteOcupado = consultaIdToIgnore == null
                ? consultaRepository.existsByPacienteIdAndDataHora(pacienteId, consulta.getDataHora())
                : consultaRepository.existsByPacienteIdAndDataHoraAndIdNot(pacienteId, consulta.getDataHora(), consultaIdToIgnore);

        if (pacienteOcupado) {
            throw new BusinessException("Paciente já possui agendamento nesse horario");
        }
    }

    private void validateOwnershipForWrite(Consulta consulta, Role role) {
        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!consulta.getPaciente().getId().equals(paciente.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode agendar consultas para voce mesmo");
            }
        }
        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));
            if (!consulta.getMedico().getId().equals(medico.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode agendar consultas para voce mesmo");
            }
        }
    }

    @Override
    public Page<ConsultaResponse> findAll(Pageable pageable, StatusConsulta status) {
        Role role = getCurrentUserRole();
        Page<Consulta> page = switch (role) {
            case ADMIN -> status != null
                    ? consultaRepository.findByStatus(status, pageable)
                    : consultaRepository.findAll(pageable);
            case MEDICO -> {
                Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
                yield status != null
                        ? consultaRepository.findAllByMedicoIdAndStatus(medico.getId(), status, pageable)
                        : consultaRepository.findAllByMedicoId(medico.getId(), pageable);
            }
            case CLIENTE -> {
                Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
                yield status != null
                        ? consultaRepository.findAllByPacienteIdAndStatus(paciente.getId(), status, pageable)
                        : consultaRepository.findAllByPacienteId(paciente.getId(), pageable);
            }
        };
        return page.map(ConsultaResponse::from);
    }

    @Override
    public List<ConsultaResponse> findAllByMedico(UUID idMedico) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN) {
            return toResponseList(consultaRepository.findAllByMedicoId(idMedico));
        }

        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Médico não encontrado"));
            if (!medico.getId().equals(idMedico)) {
                throw new AccessDeniedException("Acesso negado");
            }
            return toResponseList(consultaRepository.findAllByMedicoId(idMedico));
        }
        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public List<ConsultaResponse> findAllByPaciente(UUID idPaciente) {
        Role role = getCurrentUserRole();

        if (role == Role.ADMIN) {
            return toResponseList(consultaRepository.findAllByPacienteId(idPaciente));
        }
        if (role == Role.MEDICO) {
            return toResponseList(consultaRepository.findAllByMedicoId(idPaciente));
        }

        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!paciente.getId().equals(idPaciente)) {
                throw new AccessDeniedException("Acesso negado");
            }
            return toResponseList(consultaRepository.findAllByPacienteId(idPaciente));
        }

        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public ConsultaResponse findById(UUID id) {
        Role role = getCurrentUserRole();
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

        if (role == Role.ADMIN) {
            return ConsultaResponse.from(consulta);
        }
        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));
            if (!consulta.getMedico().getId().equals(medico.getId())) {
                throw new AccessDeniedException("Acesso negado");
            }
            return ConsultaResponse.from(consulta);
        }
        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!consulta.getPaciente().getId().equals(paciente.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode ver suas proprias consultas");
            }
            return ConsultaResponse.from(consulta);
        }

        throw new AccessDeniedException("Acesso negado");
    }

    @Override
    public void save(ConsultaRequest request) {
        Role role = getCurrentUserRole();
        Consulta consulta = buildConsulta(request);
        validateOwnershipForWrite(consulta, role);
        validateScheduleConflict(consulta, null);
        consultaRepository.save(consulta);
    }

    @Override
    public void update(UUID id, ConsultaRequest request) {
        Role role = getCurrentUserRole();

        Consulta existing = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não existente"));

        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!existing.getPaciente().getId().equals(paciente.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode alterar as proprias consultas");
            }
        }

        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));
            if (!existing.getMedico().getId().equals(medico.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode alterar as proprias consultas");
            }
        }

        Consulta consulta = buildConsulta(request);
        consulta.setId(id);
        validateScheduleConflict(consulta, id);
        consultaRepository.save(consulta);
    }

    @Override
    public void delete(UUID id) {
        Role role = getCurrentUserRole();

        Consulta existing = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não existente"));

        if (role == Role.CLIENTE) {
            Paciente paciente = pacienteRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            if (!existing.getPaciente().getId().equals(paciente.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode alterar as proprias consultas");
            }
        }

        if (role == Role.MEDICO) {
            Medico medico = medicoRepository.findByIdUserId(getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medico não encontrado"));
            if (!existing.getMedico().getId().equals(medico.getId())) {
                throw new AccessDeniedException("Acesso negado, voce só pode alterar as proprias consultas");
            }
        }

        existing.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(existing);
    }
}
