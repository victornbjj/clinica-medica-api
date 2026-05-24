package com.clinic_api.controller;

import com.clinic_api.domain.Medico;
import com.clinic_api.dto.medico.MedicoResponse;
import com.clinic_api.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    /**
     * GET /medicos
     * Lista médicos com campos conforme o papel (service aplica visibilidade e DTO):
     * - ADMIN: id, nome, especialidade, crm, email
     * - CLIENTE: id, nome, especialidade (lista completa para agendamento)
     * - MEDICO: id, nome, especialidade, crm, email — apenas o próprio cadastro
     */
    @GetMapping
    public ResponseEntity<List<MedicoResponse>> getMedicos() {
        return ResponseEntity.ok(medicoService.findAll());
    }

    /**
     * GET /medicos/{id}
     * Detalhes de um médico.
     * Regras aplicadas pelo service:
     * - ADMIN: resposta administrativa (inclui crm e email)
     * - MEDICO: resposta administrativa apenas do próprio cadastro
     * - CLIENTE: acesso negado
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> getMedicoById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicoService.findById(id));
    }

    /**
     * POST /medicos
     * Criar um novo médico
     * Segurança: apenas ADMIN pode criar
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createMedico(@RequestBody @Valid Medico medico) {
        medicoService.save(medico);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * PUT /medicos/{id}
     * Atualizar dados de um médico
     * Regras aplicadas pelo service:
     * - ADMIN: pode atualizar qualquer médico
     * - MEDICO: pode atualizar apenas a si mesmo
     * - CLIENTE: acesso negado
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMedico(
            @PathVariable UUID id,
            @RequestBody @Valid Medico medico) {
        medico.setId(id);
        medicoService.update(medico);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /medicos/{id}
     * Deletar um médico
     * Segurança: apenas ADMIN pode deletar
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedico(@PathVariable UUID id) {
        medicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
