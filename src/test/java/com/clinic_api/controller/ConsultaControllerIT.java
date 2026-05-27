package com.clinic_api.controller;

import com.clinic_api.domain.Consulta;
import com.clinic_api.domain.Paciente;
import com.clinic_api.domain.User;
import com.clinic_api.dto.consulta.ConsultaRequest;
import com.clinic_api.enums.Role;
import com.clinic_api.enums.StatusConsulta;
import com.clinic_api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsultaControllerIT extends AbstractIntegrationTest {

    @Test
    void createConsulta_deveRetornar201_quandoClienteAgendaParaSi() throws Exception {
        ConsultaRequest request = new ConsultaRequest();
        request.setDataHora(LocalDateTime.now().plusDays(1));
        request.setStatus(StatusConsulta.AGENDADA);
        request.setMedicoId(medico.getId());
        request.setPacienteId(paciente.getId());

        mockMvc.perform(post("/consultas")
                        .header("Authorization", bearerToken(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createConsulta_deveRetornar409_quandoHorarioJaOcupado() throws Exception {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2);

        Consulta existente = new Consulta();
        existente.setDataHora(dataHora);
        existente.setStatus(StatusConsulta.AGENDADA);
        existente.setMedico(medico);
        existente.setPaciente(paciente);
        consultaRepository.save(existente);

        ConsultaRequest request = new ConsultaRequest();
        request.setDataHora(dataHora);
        request.setStatus(StatusConsulta.AGENDADA);
        request.setMedicoId(medico.getId());
        request.setPacienteId(paciente.getId());

        mockMvc.perform(post("/consultas")
                        .header("Authorization", bearerToken(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createConsulta_deveRetornar403_quandoClienteTentaAgendarParaOutroPaciente() throws Exception {
        User outroCliente = createUser("outro@test.com", "senha123", Role.CLIENTE);
        Paciente outroPaciente = new Paciente();
        outroPaciente.setName("Outro");
        outroPaciente.setIdUser(outroCliente);
        outroPaciente = pacienteRepository.save(outroPaciente);

        ConsultaRequest request = new ConsultaRequest();
        request.setDataHora(LocalDateTime.now().plusDays(3));
        request.setStatus(StatusConsulta.AGENDADA);
        request.setMedicoId(medico.getId());
        request.setPacienteId(outroPaciente.getId());

        mockMvc.perform(post("/consultas")
                        .header("Authorization", bearerToken(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listConsultas_deveRetornar200_comFiltroStatus() throws Exception {
        mockMvc.perform(get("/consultas")
                        .param("status", "AGENDADA")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk());
    }
}
