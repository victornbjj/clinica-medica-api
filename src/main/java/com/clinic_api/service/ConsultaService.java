package com.clinic_api.service;

import com.clinic_api.domain.Consulta;
import com.clinic_api.dto.consulta.ConsultaResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultaService {

    List<ConsultaResponse> findAllByMedico(UUID idMedico);

    List<ConsultaResponse> findAllByPaciente(UUID idPaciente);

    List<ConsultaResponse> findAll();

    ConsultaResponse findById(UUID id);

    void save(Consulta consulta);

    void update(Consulta consulta);

    void delete(UUID id);;
}
