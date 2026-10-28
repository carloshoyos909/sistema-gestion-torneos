package com.torneos.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.PartidoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Partido;
import com.torneos.model.Torneo;
import com.torneos.repository.PartidoRepository;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final TorneoService torneoService;
    private final EquipoService equipoService;

    public PartidoService(PartidoRepository partidoRepository,
                          TorneoService torneoService,
                          EquipoService equipoService) {
        this.partidoRepository = partidoRepository;
        this.torneoService = torneoService;
        this.equipoService = equipoService;
    }

    @Transactional
    public PartidoResponse programarPartido(Long torneoId, Long equipoLocalId,
                                            Long equipoVisitanteId, LocalDate fecha,
                                            LocalTime hora, String cancha) {
        if (equipoLocalId.equals(equipoVisitanteId)) {
            throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo.");
        }
        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);
        Equipo local = equipoService.buscarEntidadPorId(torneoId, equipoLocalId);
        Equipo visitante = equipoService.buscarEntidadPorId(torneoId, equipoVisitanteId);

        return toResponse(partidoRepository.save(
                new Partido(torneo, local, visitante, fecha, hora, cancha.trim())));
    }

    @Transactional
    public PartidoResponse registrarResultado(Long partidoId, int golesLocal, int golesVisitante) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado."));
        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setJugado(true);
        return toResponse(partidoRepository.save(partido));
    }

    @Transactional(readOnly = true)
    public List<PartidoResponse> getPartidos(Long torneoId) {
        torneoService.buscarEntidadPorId(torneoId);
        return partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId).stream()
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
                p.getGolesVisitante()
        );
    }
}
