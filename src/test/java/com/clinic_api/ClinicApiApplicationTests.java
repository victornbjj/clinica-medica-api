package com.clinic_api;

import com.clinic_api.service.CepService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ClinicApiApplicationTests {

    @MockBean
    private CepService cepService;

    @Test
    void contextLoads() {
    }
}
