package com.clinic_api.dto.user;

import com.clinic_api.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank
    @Email
    private String email;

    private String password;

    private Role role;
}
