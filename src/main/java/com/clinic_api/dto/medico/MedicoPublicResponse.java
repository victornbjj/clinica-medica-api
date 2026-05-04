package com.clinic_api.dto.medico;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MedicoPublicResponse {
    private UUID id;
    private String nome;
    private String especialidade;
}
