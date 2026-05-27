package com.clinic_api.dto.paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PacienteRequest {

    @NotBlank
    private String name;

    @NotNull
    private UUID userId;
}
