package com.clinic_api.controller;

import com.clinic_api.dto.LoginRequest;
import com.clinic_api.dto.RegisterRequest;
import com.clinic_api.exception.BusinessException;
import com.clinic_api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void login_deveRetornarToken_quandoCredenciaisValidas() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("cliente@test.com");
        request.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void login_deveRetornar404_quandoUsuarioNaoExiste() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@test.com");
        request.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_deveRetornar201_quandoDadosValidos() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("novo@test.com");
        request.setPassword("senha123");
        request.setNome("Novo Cliente");
        request.setCep("01310100");
        request.setNumero("100");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_deveRetornar409_quandoCepInvalido() throws Exception {
        when(cepService.consultarCep("00000000"))
                .thenThrow(new BusinessException("CEP não encontrado"));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("cep-invalido@test.com");
        request.setPassword("senha123");
        request.setNome("Cliente CEP");
        request.setCep("00000000");
        request.setNumero("10");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CEP não encontrado"));
    }
}
