package com.clinic_api.service;

import com.clinic_api.dto.consulta.ConsultaRequest;
import com.clinic_api.dto.consulta.ConsultaResponse;
import com.clinic_api.enums.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ConsultaService {

    Page<ConsultaResponse> findAll(Pageable pageable, StatusConsulta status);

    List<ConsultaResponse> findAllByMedico(UUID idMedico);

    List<ConsultaResponse> findAllByPaciente(UUID idPaciente);

    ConsultaResponse findById(UUID id);

    void save(ConsultaRequest request);

    void update(UUID id, ConsultaRequest request);

    void delete(UUID id);
}
