package com.clinic_api.dto.paciente;

import com.clinic_api.domain.Paciente;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PacienteResponse {

    private UUID id;
    private String name;
    private String email;

    public static PacienteResponse from(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getName(),
                paciente.getIdUser() != null ? paciente.getIdUser().getEmail() : null
        );
    }
}
