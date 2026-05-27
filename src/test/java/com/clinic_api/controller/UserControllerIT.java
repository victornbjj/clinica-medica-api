package com.clinic_api.controller;

import com.clinic_api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends AbstractIntegrationTest {

    @Test
    void listUsers_deveRetornar403_quandoCliente() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", bearerToken(cliente))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_deveRetornar200_quandoAdmin() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", bearerToken(admin))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
