package com.clinic_api.service;

import com.clinic_api.client.ViaCepClient;
import com.clinic_api.domain.Endereco;
import com.clinic_api.dto.viacep.ViaCepApiResponse;
import com.clinic_api.exception.BusinessException;
import feign.FeignException;
import org.springframework.stereotype.Service;

@Service
public class CepService {

    private final ViaCepClient viaCepClient;

    public CepService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public Endereco consultarCep(String cep) {
        String cepNormalizado = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepNormalizado.length() != 8) {
            throw new BusinessException("CEP inválido");
        }

        ViaCepApiResponse response;
        try {
            response = viaCepClient.consultarCep(cepNormalizado);
        } catch (FeignException e) {
            throw new BusinessException("CEP inválido ou não encontrado");
        }

        if (response == null || response.hasErro()
                || response.getLogradouro() == null
                || response.getLocalidade() == null
                || response.getUf() == null) {
            throw new BusinessException("CEP não encontrado");
        }

        Endereco endereco = new Endereco();
        endereco.setCep(response.getCep());
        endereco.setRua(response.getLogradouro());
        endereco.setCidade(response.getLocalidade());
        endereco.setEstado(response.getUf());
        endereco.setComplemento(response.getComplemento());
        return endereco;
    }
}
