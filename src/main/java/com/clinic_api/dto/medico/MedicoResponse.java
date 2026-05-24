package com.clinic_api.dto.medico;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Resposta unificada para listagem e detalhe de médicos.
 * Campos administrativos (crm, email) são preenchidos apenas para ADMIN e MEDICO (próprio cadastro).
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicoResponse {
    private UUID id;
    private String nome;
    private String especialidade;
    private String crm;
    private String email;
}
