package com.clinic_api.service;

import com.clinic_api.dto.medico.MedicoRequest;
import com.clinic_api.dto.medico.MedicoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicoService {

    Page<MedicoResponse> findAll(Pageable pageable, String especialidade);

    MedicoResponse findById(UUID id);

    void save(MedicoRequest request);

    void update(UUID id, MedicoRequest request);

    void delete(UUID id);
}
