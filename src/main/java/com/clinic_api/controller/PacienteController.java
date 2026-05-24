package com.clinic_api.controller;

import com.clinic_api.domain.Paciente;
import com.clinic_api.dto.paciente.PacienteResponse;
import com.clinic_api.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * GET /pacientes
     * Lista pacientes conforme o papel do usuário autenticado.
     * Regras aplicadas pelo service:
     * - ADMIN e MEDICO: todos os pacientes
     * - CLIENTE: apenas o próprio cadastro
     */
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> getPacientes() {
        return ResponseEntity.ok(pacienteService.findAll());
    }

    /**
     * GET /pacientes/{id}
     * Detalhes de um paciente.
     * Regras aplicadas pelo service:
     * - ADMIN e MEDICO: qualquer paciente
     * - CLIENTE: apenas o próprio cadastro
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> getPacienteById(@PathVariable UUID id) {
        return ResponseEntity.ok(pacienteService.findById(id));
    }

    /**
     * POST /pacientes
     * Cadastra um novo paciente.
     * Regras aplicadas pelo service: apenas ADMIN e MEDICO.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public ResponseEntity<Void> createPaciente(@RequestBody @Valid Paciente paciente) {
        pacienteService.save(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * PUT /pacientes/{id}
     * Atualiza dados de um paciente.
     * Regras aplicadas pelo service:
     * - ADMIN e MEDICO: qualquer paciente
     * - CLIENTE: apenas o próprio cadastro
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePaciente(
            @PathVariable UUID id,
            @RequestBody @Valid Paciente paciente) {
        paciente.setId(id);
        pacienteService.update(paciente);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /pacientes/{id}
     * Remove um paciente.
     * Segurança: apenas ADMIN pode deletar.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePaciente(@PathVariable UUID id) {
        pacienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
