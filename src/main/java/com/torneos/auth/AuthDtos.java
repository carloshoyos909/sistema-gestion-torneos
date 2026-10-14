package com.torneos.auth;

import jakarta.validation.constraints.*;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 80)
            String nombre,

            @NotBlank @Email @Size(max = 120)
            String email,

            // BCrypt solo usa los primeros 72 bytes, por eso el máximo es 72
            @NotBlank
            @Size(min = 10, max = 72, message = "La contraseña debe tener entre 10 y 72 caracteres")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                     message = "La contraseña debe incluir al menos una letra y un número")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 72) String password
    ) {}

    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {}

    public record UserResponse(Long id, String nombre, String email, Role role) {}
}
