package com.clinic_api.dto.medico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MedicoRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String crm;

    @NotBlank
    private String especialidade;

    @NotNull
    private UUID userId;
}
