package com.clinic_api.controller;

import com.clinic_api.dto.consulta.ConsultaRequest;
import com.clinic_api.dto.consulta.ConsultaResponse;
import com.clinic_api.enums.StatusConsulta;
import com.clinic_api.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    public ResponseEntity<Page<ConsultaResponse>> getConsultas(
            @RequestParam(required = false) StatusConsulta status,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(consultaService.findAll(pageable, status));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<ConsultaResponse>> getConsultasByMedico(@PathVariable UUID medicoId) {
        return ResponseEntity.ok(consultaService.findAllByMedico(medicoId));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaResponse>> getConsultasByPaciente(@PathVariable UUID pacienteId) {
        return ResponseEntity.ok(consultaService.findAllByPaciente(pacienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> getConsultaById(@PathVariable UUID id) {
        return ResponseEntity.ok(consultaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody @Valid ConsultaRequest request) {
        consultaService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateConsulta(
            @PathVariable UUID id,
            @RequestBody @Valid ConsultaRequest request) {
        consultaService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsulta(@PathVariable UUID id) {
        consultaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
