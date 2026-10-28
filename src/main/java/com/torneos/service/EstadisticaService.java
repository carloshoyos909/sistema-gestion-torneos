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

    @Transactional
    public List<EstadisticaResponse> getEstadisticas(Long torneoId, String filtroNombre) {
        crearEstadisticasFaltantes(torneoId);

        List<Estadistica> lista;
        if (filtroNombre != null && !filtroNombre.isBlank()) {
            lista = estadisticaRepository.buscarPorTorneoYNombreJugador(torneoId, filtroNombre.trim());
        } else {
            lista = estadisticaRepository.findByTorneoIdOrderByGolesDescAsistenciasDesc(torneoId);
        }
        return lista.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Jugador> getJugadores(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));
        return jugadorRepository.findByEquipoTorneoIdOrderByNombreAsc(torneo.getId());
    }

    private void crearEstadisticasFaltantes(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));

        List<Estadistica> nuevas = jugadorRepository.findByEquipoTorneoIdOrderByNombreAsc(torneoId).stream()
                .filter(jugador -> estadisticaRepository
                        .findByJugadorIdAndTorneoId(jugador.getId(), torneoId).isEmpty())
                .map(jugador -> new Estadistica(jugador, torneo))
                .toList();

        if (!nuevas.isEmpty()) {
            estadisticaRepository.saveAll(nuevas);
        }
    }

    @Transactional
    public EstadisticaResponse actualizarEstadistica(Long jugadorId, Long torneoId,
                                                      int goles, int asistencias,
                                                      int partidosJugados,
                                                      int tarjetasAmarillas, int tarjetasRojas) {
        if (goles < 0 || asistencias < 0 || partidosJugados < 0
                || tarjetasAmarillas < 0 || tarjetasRojas < 0) {
            throw new IllegalArgumentException("Las estadísticas no pueden tener valores negativos.");
        }

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado."));
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado."));
        if (!jugador.getEquipo().getTorneo().getId().equals(torneo.getId())) {
            throw new IllegalArgumentException("El jugador no pertenece al torneo seleccionado.");
        }

        Estadistica est = estadisticaRepository
                .findByJugadorIdAndTorneoId(jugadorId, torneoId)
                .orElseGet(() -> new Estadistica(jugador, torneo));

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
