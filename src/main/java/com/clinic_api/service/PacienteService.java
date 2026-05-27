package com.clinic_api.service;

import com.clinic_api.dto.paciente.PacienteRequest;
import com.clinic_api.dto.paciente.PacienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PacienteService {

    Page<PacienteResponse> findAll(Pageable pageable);

    PacienteResponse findById(UUID id);

    void save(PacienteRequest request);

    void update(UUID id, PacienteRequest request);

    void delete(UUID id);
}
