package com.torneos.service;

import com.torneos.model.Torneo;
import com.torneos.model.Usuario;
import com.torneos.repository.TorneoRepository;
import com.torneos.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final UsuarioRepository usuarioRepository;

    public TorneoService(TorneoRepository torneoRepository, UsuarioRepository usuarioRepository) {
        this.torneoRepository = torneoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    @Transactional(readOnly = true)
    public Torneo obtenerTorneoPorId(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        Usuario usuarioActual = getUsuarioActual();

        // Validación IDOR para VISUALIZACIÓN (Lectura)
        if (!torneo.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No tienes permisos para visualizar este torneo.");
        }

        return torneo;
    }

    @Transactional
    public void eliminarTorneo(Long torneoId) {
        // Reutilizamos la validación de propiedad o la aplicamos directamente
        Torneo torneo = obtenerTorneoPorId(torneoId);

        torneoRepository.delete(torneo);
    }
}