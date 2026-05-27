package com.clinic_api.controller;

import com.clinic_api.dto.medico.MedicoRequest;
import com.clinic_api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicoControllerIT extends AbstractIntegrationTest {

    @Test
    void listMedicos_deveOcultarCrmParaCliente() throws Exception {
        mockMvc.perform(get("/medicos")
                        .header("Authorization", bearerToken(cliente))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Dr. Teste"))
                .andExpect(jsonPath("$.content[0].crm").value(nullValue()));
    }

    @Test
    void listMedicos_deveExporCrmParaAdmin() throws Exception {
        mockMvc.perform(get("/medicos")
                        .header("Authorization", bearerToken(admin))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].crm").value("CRM-001"));
    }

    @Test
    void listMedicos_deveFiltrarPorEspecialidade() throws Exception {
        mockMvc.perform(get("/medicos")
                        .header("Authorization", bearerToken(admin))
                        .param("especialidade", "Cardio")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].especialidade").value("Cardiologia"));
    }

    @Test
    void createMedico_deveRetornar403_quandoNaoAdmin() throws Exception {
        MedicoRequest request = new MedicoRequest();
        request.setNome("Dr. Novo");
        request.setCrm("CRM-999");
        request.setEspecialidade("Dermatologia");
        request.setUserId(admin.getId());

        mockMvc.perform(post("/medicos")
                        .header("Authorization", bearerToken(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMedicoById_deveRetornar403_paraCliente() throws Exception {
        mockMvc.perform(get("/medicos/{id}", medico.getId())
                        .header("Authorization", bearerToken(cliente)))
                .andExpect(status().isForbidden());
    }
}
