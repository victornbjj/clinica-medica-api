package com.clinic_api.service;

import com.clinic_api.domain.Endereco;

public interface CepService {

    Endereco consultarCep(String cep);
}
