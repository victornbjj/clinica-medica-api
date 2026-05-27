package com.clinic_api.controller;

import com.clinic_api.dto.medico.MedicoRequest;
import com.clinic_api.dto.medico.MedicoResponse;
import com.clinic_api.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<Page<MedicoResponse>> getMedicos(
            @RequestParam(required = false) String especialidade,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(medicoService.findAll(pageable, especialidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> getMedicoById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createMedico(@RequestBody @Valid MedicoRequest request) {
        medicoService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMedico(
            @PathVariable UUID id,
            @RequestBody @Valid MedicoRequest request) {
        medicoService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedico(@PathVariable UUID id) {
        medicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
