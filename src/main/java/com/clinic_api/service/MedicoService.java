package com.clinic_api.service;

import com.clinic_api.domain.Medico;

import java.util.List;
import java.util.UUID;

public interface MedicoService {

    List<Medico> findAll();

    Medico findById(UUID id);

    void save(Medico medico);

    void update(Medico medico);

    void delete(UUID id);
}
