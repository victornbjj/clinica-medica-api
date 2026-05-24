package com.clinic_api.service;


import com.clinic_api.domain.Paciente;
import com.clinic_api.dto.paciente.PacienteResponse;

import java.util.List;
import java.util.UUID;

public interface PacienteService {
    List<PacienteResponse> findAll();

    PacienteResponse findById(UUID id);

    void save(Paciente paciente);

    void update(Paciente paciente);

    void delete(UUID id);
}
