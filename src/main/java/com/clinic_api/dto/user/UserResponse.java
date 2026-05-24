package com.clinic_api.dto.user;

import com.clinic_api.domain.User;
import com.clinic_api.dto.endereco.EnderecoResponse;
import com.clinic_api.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private Role role;
    private EnderecoResponse endereco;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                EnderecoResponse.from(user.getEndereco())
        );
    }
}
