package com.torneos.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.PartidoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Partido;
import com.torneos.model.Torneo;
import com.torneos.repository.PartidoRepository;
import com.torneos.repository.UsuarioRepository;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final TorneoService torneoService;
    private final EquipoService equipoService;
    private final UsuarioRepository usuarioRepository;

    public PartidoService(PartidoRepository partidoRepository,
                          TorneoService torneoService,
                          EquipoService equipoService,
                          UsuarioRepository usuarioRepository) {
        this.partidoRepository = partidoRepository;
        this.torneoService = torneoService;
        this.equipoService = equipoService;
        this.usuarioRepository = usuarioRepository;
    }

    // ── Programar partido — valida propietario del torneo ─────────────────
    @Transactional
    public PartidoResponse programarPartido(Long torneoId, Long equipoLocalId,
                                            Long equipoVisitanteId, LocalDate fecha,
                                            LocalTime hora, String cancha) {
        if (equipoLocalId.equals(equipoVisitanteId)) {
            throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo.");
        }
        // Solo ADMIN o el organizador dueño del torneo puede programar partidos
        torneoService.validarPropietario(torneoId);

        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);
        Equipo local = equipoService.buscarEntidadPorId(torneoId, equipoLocalId);
        Equipo visitante = equipoService.buscarEntidadPorId(torneoId, equipoVisitanteId);

        return toResponse(partidoRepository.save(
                new Partido(torneo, local, visitante, fecha, hora, cancha.trim())));
    }

    // ── Registrar resultado — valida propietario del torneo ───────────────
    @Transactional
    public PartidoResponse registrarResultado(Long partidoId, int golesLocal, int golesVisitante) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado."));

        // Valida que el usuario sea ADMIN o dueño del torneo del partido
        torneoService.validarPropietario(partido.getTorneo().getId());

        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setJugado(true);
        return toResponse(partidoRepository.save(partido));
    }

    // ── Eliminar partido — ADMIN o dueño del torneo ───────────────────────
    @Transactional
    public void eliminarPartido(Long partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado."));

        torneoService.validarPropietario(partido.getTorneo().getId());
        partidoRepository.delete(partido);
    }

    @Transactional(readOnly = true)
    public List<PartidoResponse> getPartidos(Long torneoId) {
        return partidoRepository.findByTorneoIdWithEquipos(torneoId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PartidoResponse toResponse(Partido p) {
        return new PartidoResponse(
                p.getId(),
                p.getEquipoLocal().getId(),
                p.getEquipoLocal().getNombre(),
                p.getEquipoVisitante().getId(),
                p.getEquipoVisitante().getNombre(),
                p.getFecha(),
                p.getHora(),
                p.getCancha(),
                p.isJugado(),
                p.getGolesLocal(),
                p.getGolesVisitante(),
                p.getFase(),
                p.getRonda()
        );
    }
}
