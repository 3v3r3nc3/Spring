package ru.mtuci.everence.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String email;
    private TokenResponse tokens;
    private String username;
}
