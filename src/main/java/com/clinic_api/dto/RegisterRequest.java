package com.clinic_api.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String email;
    private String password;
    private String nome;
    private String cep;
    private String numero;

}
