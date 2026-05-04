package com.clinic_api.repository;

import com.clinic_api.domain.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {
    List<Consulta> findAllByMedicoId(UUID idMedico);

    List<Consulta> findAllByPacienteId(UUID idPaciente);

    boolean existsByMedicoIdAndDataHora(UUID medicoId, LocalDateTime dataHora);

    boolean existsByPacienteIdAndDataHora(UUID pacienteId, LocalDateTime dataHora);

    boolean existsByMedicoIdAndDataHoraAndIdNot(UUID medicoId, LocalDateTime dataHora, UUID id);

    boolean existsByPacienteIdAndDataHoraAndIdNot(UUID pacienteId, LocalDateTime dataHora, UUID id);

}
