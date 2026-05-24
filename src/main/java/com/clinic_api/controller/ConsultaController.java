package com.clinic_api.controller;

import com.clinic_api.domain.Consulta;
import com.clinic_api.dto.consulta.ConsultaResponse;
import com.clinic_api.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /**
     * GET /consultas
     * Lista consultas conforme o papel do usuário autenticado.
     * Regras aplicadas pelo service:
     * - ADMIN: todas as consultas
     * - MEDICO: consultas do médico logado
     * - CLIENTE: consultas do paciente logado
     */
    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> getConsultas() {
        return ResponseEntity.ok(consultaService.findAll());
    }

    /**
     * GET /consultas/medico/{medicoId}
     * Lista consultas de um médico.
     * Regras aplicadas pelo service:
     * - ADMIN: qualquer médico
     * - MEDICO: apenas o próprio médico
     */
    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<ConsultaResponse>> getConsultasByMedico(@PathVariable UUID medicoId) {
        return ResponseEntity.ok(consultaService.findAllByMedico(medicoId));
    }

    /**
     * GET /consultas/paciente/{pacienteId}
     * Lista consultas de um paciente.
     * Regras aplicadas pelo service:
     * - ADMIN: qualquer paciente
     * - MEDICO: consultas vinculadas ao médico (filtro no service)
     * - CLIENTE: apenas o próprio paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaResponse>> getConsultasByPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(consultaService.findAllByPaciente(pacienteId));
    }

    /**
     * GET /consultas/{id}
     * Detalhes de uma consulta.
     * Regras aplicadas pelo service conforme ADMIN, MEDICO ou CLIENTE.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> getConsultaById(@PathVariable UUID id) {
        return ResponseEntity.ok(consultaService.findById(id));
    }

    /**
     * POST /consultas
     * Agenda uma nova consulta.
     * Regras de identidade e conflito de horário aplicadas pelo service.
     */
    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody @Valid Consulta consulta) {
        consultaService.save(consulta);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * PUT /consultas/{id}
     * Atualiza uma consulta existente.
     * Regras aplicadas pelo service conforme o papel do usuário.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateConsulta(
            @PathVariable UUID id,
            @RequestBody @Valid Consulta consulta) {
        consulta.setId(id);
        consultaService.update(consulta);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /consultas/{id}
     * Cancela uma consulta (status CANCELADA).
     * Regras aplicadas pelo service conforme o papel do usuário.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsulta(@PathVariable UUID id) {
        consultaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
