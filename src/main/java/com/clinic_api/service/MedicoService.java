package com.clinic_api.service;

import com.clinic_api.domain.Medico;
import com.clinic_api.dto.medico.MedicoResponse;

import java.util.List;
import java.util.UUID;

public interface MedicoService {

    List<MedicoResponse> findAll();

    MedicoResponse findById(UUID id);

    void save(Medico medico);

    void update(Medico medico);

    void delete(UUID id);
}
