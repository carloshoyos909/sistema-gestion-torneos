package com.torneos.security;

import com.torneos.model.Rol;
import com.torneos.model.Usuario;
import com.torneos.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearUsuarioSiNoExiste("admin", "12345", Rol.ADMIN);
        crearUsuarioSiNoExiste("organizador1", "12345", Rol.ORGANIZADOR);
        crearUsuarioSiNoExiste("jugador1", "12345", Rol.JUGADOR);
    }

    private void crearUsuarioSiNoExiste(
            String username, String password, Rol rol) {

        if (usuarioRepository.findByUsername(username).isEmpty()) {
            Usuario usuario = new Usuario(
                    username,
                    passwordEncoder.encode(password),
                    rol
            );

            usuarioRepository.save(usuario);
        }
    }
}