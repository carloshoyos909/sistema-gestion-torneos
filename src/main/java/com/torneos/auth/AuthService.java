package com.torneos.auth;

import com.torneos.auth.AuthDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService attempts;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       JwtService jwt, LoginAttemptService attempts) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.attempts = attempts;
    }

    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
        }
        // El rol se asigna en el servidor: nunca se acepta desde el cliente
        User saved = users.save(new User(req.nombre().trim(), email,
                encoder.encode(req.password()), Role.JUGADOR));
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);

        if (attempts.isBlocked(email)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intenta de nuevo mas tarde");
        }

        User user = users.findByEmail(email).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPasswordHash())) {
            attempts.loginFailed(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        attempts.loginSucceeded(email);
        return new AuthResponse(jwt.generate(user), "Bearer", jwt.getExpirationSeconds());
    }

    public UserResponse me(String email) {
        return users.findByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRole());
    }
}