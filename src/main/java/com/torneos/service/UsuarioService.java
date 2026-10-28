package com.torneos.service;

import com.torneos.model.Rol;
import com.torneos.model.Usuario;
import com.torneos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    @Transactional
    public void cambiarRol(Long usuarioId, String usernameActual, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (usuario.getUsername().equals(usernameActual)) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        }

        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (passwordNueva == null || passwordNueva.length() < 4) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 4 caracteres.");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarUsuario(String username, String password, Rol rol) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (usuarioRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }

        Usuario nuevo = new Usuario(username.trim(), passwordEncoder.encode(password), rol);
        return usuarioRepository.save(nuevo);
    }

    @Transactional
    public void eliminarUsuario(Long usuarioId, String usernameActual) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (usuario.getUsername().equals(usernameActual)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }

        usuarioRepository.delete(usuario);
    }
}
