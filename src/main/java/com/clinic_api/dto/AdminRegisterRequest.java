package com.clinic_api.dto;


import com.clinic_api.enums.Role;
import lombok.Data;


@Data
public class AdminRegisterRequest {
    private String email;
    private String password;
    private String nome;
    private String cep;
    private String numero;
    private Role role;
    private String crm;
    private String especialidade;


}
