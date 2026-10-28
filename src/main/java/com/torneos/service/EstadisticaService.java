package com.torneos.service;

import com.torneos.dto.EstadisticaResponse;
import com.torneos.model.Estadistica;
import com.torneos.model.Jugador;
import com.torneos.model.Torneo;
import com.torneos.repository.EstadisticaRepository;
import com.torneos.repository.JugadorRepository;
import com.torneos.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadisticaService {

    private final EstadisticaRepository estadisticaRepository;
    private final JugadorRepository jugadorRepository;
    private final TorneoRepository torneoRepository;

    public EstadisticaService(EstadisticaRepository estadisticaRepository,
                               JugadorRepository jugadorRepository,
                               TorneoRepository torneoRepository) {
        this.estadisticaRepository = estadisticaRepository;
        this.jugadorRepository = jugadorRepository;
        this.torneoRepository = torneoRepository;
    }

    @Transactional(readOnly = true)
    public List<EstadisticaResponse> getEstadisticas(Long torneoId, String filtroNombre) {
        List<Estadistica> lista;
        if (filtroNombre != null && !filtroNombre.isBlank()) {
            lista = estadisticaRepository.buscarPorTorneoYNombreJugador(torneoId, filtroNombre.trim());
        } else {
            lista = estadisticaRepository.findByTorneoIdOrderByGolesDescAsistenciasDesc(torneoId);
        }
        return lista.stream().map(this::toResponse).toList();
    }

    @Transactional
    public EstadisticaResponse actualizarEstadistica(Long jugadorId, Long torneoId,
                                                      int goles, int asistencias,
                                                      int partidosJugados,
                                                      int tarjetasAmarillas, int tarjetasRojas) {
        Estadistica est = estadisticaRepository
                .findByJugadorIdAndTorneoId(jugadorId, torneoId)
                .orElseGet(() -> {
                    Jugador jugador = jugadorRepository.findById(jugadorId)
                            .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado."));
                    Torneo torneo = torneoRepository.findById(torneoId)
                            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado."));
                    return new Estadistica(jugador, torneo);
                });

        est.setGoles(goles);
        est.setAsistencias(asistencias);
        est.setPartidosJugados(partidosJugados);
        est.setTarjetasAmarillas(tarjetasAmarillas);
        est.setTarjetasRojas(tarjetasRojas);
        return toResponse(estadisticaRepository.save(est));
    }

    private EstadisticaResponse toResponse(Estadistica e) {
        return new EstadisticaResponse(
                e.getId(),
                e.getJugador().getId(),
                e.getJugador().getNombre(),
                e.getJugador().getEquipo().getNombre(),
                e.getGoles(),
                e.getAsistencias(),
                e.getPartidosJugados(),
                e.getTarjetasAmarillas(),
                e.getTarjetasRojas()
        );
    }
}
