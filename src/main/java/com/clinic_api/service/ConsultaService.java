package com.clinic_api.service;

import com.clinic_api.domain.Consulta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultaService {

    List<Consulta> findAllByMedico(UUID idMedico);

    List<Consulta> findAllByPaciente(UUID idPaciente);

    List<Consulta> findAll();

    Consulta findById(UUID id);

    void save(Consulta consulta);

    void update(Consulta consulta);

    void delete(UUID id);;
}
