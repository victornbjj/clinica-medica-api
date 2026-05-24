package com.clinic_api.dto.consulta;

import com.clinic_api.domain.Consulta;
import com.clinic_api.enums.StatusConsulta;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ConsultaResponse {

    private UUID id;
    private LocalDateTime dataHora;
    private StatusConsulta status;
    private UUID medicoId;
    private String medicoNome;
    private UUID pacienteId;
    private String pacienteNome;

    public static ConsultaResponse from(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getMedico().getId(),
                consulta.getMedico().getNome(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getName()
        );
    }
}
