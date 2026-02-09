package com.org.dermavera.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    private String userId;
    private String userName;
    private String password;
}
