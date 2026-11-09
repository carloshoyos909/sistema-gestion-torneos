package com.torneos.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.TorneoResponse;
import com.torneos.model.FormatoTorneo;
import com.torneos.model.Rol;
import com.torneos.model.Torneo;
import com.torneos.model.Usuario;
import com.torneos.repository.EquipoRepository;
import com.torneos.repository.PartidoRepository;
import com.torneos.repository.TorneoRepository;
import com.torneos.repository.UsuarioRepository;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    public TorneoService(TorneoRepository torneoRepository,
                         PartidoRepository partidoRepository,
                         EquipoRepository equipoRepository,
                         UsuarioRepository usuarioRepository) {
        this.torneoRepository = torneoRepository;
        this.partidoRepository = partidoRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── Crear torneo asignando el usuario autenticado como creador ────────────
    @Transactional
    public TorneoResponse crearTorneo(String nombre, String deporte,
                                       String categoria, FormatoTorneo formato) {
        String nombreNormalizado = nombre.trim();
        if (torneoRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("El nombre del torneo ya se encuentra registrado.");
        }
        Usuario creador = getUsuarioAutenticado();
        Torneo torneo = new Torneo(nombreNormalizado, deporte.trim(),
                categoria.trim(), formato, creador);
        return toResponse(torneoRepository.save(torneo));
    }

    @Transactional
    public TorneoResponse crearTorneo(String nombre, String deporte, String categoria) {
        return crearTorneo(nombre, deporte, categoria, FormatoTorneo.LIGA);
    }

    // ── Todos los torneos (solo ADMIN) ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneos() {
        return torneoRepository.findAll().stream().map(this::toResponse).toList();
    }

    // ── Torneos activos — TODOS los roles ven TODOS los torneos activos ──────
    // La restricción de propietario aplica solo al editar, no al consultar
    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneosActivos() {
        return torneoRepository.findAll().stream()
                .filter(Torneo::isActivo)
                .map(this::toResponse)
                .toList();
    }

    // ── Torneos visibles para el usuario actual en el Panel Organizador ───────
    // ADMIN → todos | ORGANIZADOR → solo los suyos
    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneosParaGestion() {
        Usuario usuario = getUsuarioAutenticado();

        if (usuario == null || usuario.getRol() == Rol.ADMIN) {
            return torneoRepository.findAll().stream().map(this::toResponse).toList();
        }

        return torneoRepository
                .findByCreadorUsername(usuario.getUsername())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Torneo buscarEntidadPorId(Long id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));
    }

    // ── Validar que el usuario actual puede modificar el torneo ──────────────
    @Transactional(readOnly = true)
    public void validarPropietario(Long torneoId) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) throw new SecurityException("No autenticado.");
        if (usuario.getRol() == Rol.ADMIN) return;

        Torneo torneo = buscarEntidadPorId(torneoId);

        // Si el torneo no tiene creador asignado (datos migrados antes de V5),
        // solo el ADMIN puede modificarlo
        if (torneo.getCreador() == null) {
            throw new SecurityException(
                    "El torneo \"" + torneo.getNombre() + "\" no tiene propietario asignado. " +
                    "Solo el Administrador puede modificarlo.");
        }

        if (!torneo.getCreador().getUsername().equals(usuario.getUsername())) {
            throw new SecurityException(
                    "No tienes permiso para modificar el torneo \"" + torneo.getNombre() + "\".");
        }
    }

    // ── Eliminar fixture — ADMIN o propietario del torneo ────────────────────
    @Transactional
    public void eliminarFixture(Long torneoId) {
        validarPropietario(torneoId);
        List<com.torneos.model.Partido> partidos =
                partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId);
        partidoRepository.deleteAll(partidos);
    }

    // ── Eliminar torneo completo — SOLO ADMIN ─────────────────────────────────
    @Transactional
    public void eliminarTorneo(Long torneoId) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null || usuario.getRol() != Rol.ADMIN) {
            throw new SecurityException("Solo el Administrador del sistema puede eliminar torneos completos.");
        }
        Torneo torneo = buscarEntidadPorId(torneoId);
        List<com.torneos.model.Partido> partidos =
                partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId);
        partidoRepository.deleteAll(partidos);
        List<com.torneos.model.Equipo> equipos =
                equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId);
        equipoRepository.deleteAll(equipos);
        torneoRepository.delete(torneo);
    }

    // ── Obtener usuario autenticado ───────────────────────────────────────────
    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return usuarioRepository.findByUsername(auth.getName()).orElse(null);
    }

    private TorneoResponse toResponse(Torneo torneo) {
        return new TorneoResponse(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getDeporte(),
                torneo.getCategoria(),
                torneo.isActivo(),
                torneo.getFormato(),
                torneo.getCreador() != null ? torneo.getCreador().getUsername() : "sistema"
        );
    }
}
