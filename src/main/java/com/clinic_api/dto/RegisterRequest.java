package com.clinic_api.dto;

import com.clinic_api.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;


    private String nome;
    private String crm;
    private String especialidade;


    private String nomePaciente;
}
