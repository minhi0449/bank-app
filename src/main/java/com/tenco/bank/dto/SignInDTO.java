package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SignInDTO {
    private String username;
    private String password;

    public User toUser(){
        return User.builder()
                .username(username)
                .password(password)
                .build();
    }
}

