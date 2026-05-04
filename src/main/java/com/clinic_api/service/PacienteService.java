package com.clinic_api.service;


import com.clinic_api.domain.Paciente;

import java.util.List;
import java.util.UUID;

public interface PacienteService {
    List<Paciente> findAll();

    Paciente findById(UUID id);

    void save(Paciente paciente);

    void update(Paciente paciente);

    void delete(UUID id);
}
