package com.clinic_api.dto.consulta;

import com.clinic_api.enums.StatusConsulta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConsultaRequest {

    @NotNull
    private LocalDateTime dataHora;

    @NotNull
    private StatusConsulta status;

    @NotNull
    private UUID medicoId;

    @NotNull
    private UUID pacienteId;
}
