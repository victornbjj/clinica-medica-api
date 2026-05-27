package com.clinic_api.repository;

import com.clinic_api.domain.Consulta;
import com.clinic_api.enums.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    List<Consulta> findAllByMedicoId(UUID idMedico);

    List<Consulta> findAllByPacienteId(UUID idPaciente);

    Page<Consulta> findAllByMedicoId(UUID idMedico, Pageable pageable);

    Page<Consulta> findAllByPacienteId(UUID idPaciente, Pageable pageable);

    Page<Consulta> findByStatus(StatusConsulta status, Pageable pageable);

    Page<Consulta> findAllByMedicoIdAndStatus(UUID idMedico, StatusConsulta status, Pageable pageable);

    Page<Consulta> findAllByPacienteIdAndStatus(UUID idPaciente, StatusConsulta status, Pageable pageable);

    boolean existsByMedicoIdAndDataHora(UUID medicoId, LocalDateTime dataHora);

    boolean existsByPacienteIdAndDataHora(UUID pacienteId, LocalDateTime dataHora);

    boolean existsByMedicoIdAndDataHoraAndIdNot(UUID medicoId, LocalDateTime dataHora, UUID id);

    boolean existsByPacienteIdAndDataHoraAndIdNot(UUID pacienteId, LocalDateTime dataHora, UUID id);
}
