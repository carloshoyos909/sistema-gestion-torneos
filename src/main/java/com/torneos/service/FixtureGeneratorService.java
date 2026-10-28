package com.torneos.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.PartidoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Partido;
import com.torneos.model.Torneo;
import com.torneos.repository.PartidoRepository;

@Service
public class FixtureGeneratorService {

    private final PartidoRepository partidoRepository;
    private final TorneoService torneoService;
    private final EquipoService equipoService;

    public FixtureGeneratorService(PartidoRepository partidoRepository,
                                    TorneoService torneoService,
                                    EquipoService equipoService) {
        this.partidoRepository = partidoRepository;
        this.torneoService = torneoService;
        this.equipoService = equipoService;
    }

    @Transactional
    public List<PartidoResponse> generarFixture(Long torneoId, LocalDate fechaInicio,
                                                 LocalTime hora, String cancha) {
        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);
        List<Equipo> equipos = equipoService.getEntidades(torneoId);

        if (equipos.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 equipos para generar el fixture.");
        }

        List<Partido> partidos = switch (torneo.getFormato()) {
            case LIGA -> generarLiga(torneo, equipos, fechaInicio, hora, cancha);
            case ELIMINATORIA_SIMPLE -> generarEliminatoria(torneo, equipos, fechaInicio, hora, cancha, false);
            case ELIMINATORIA_IDA_VUELTA -> generarEliminatoria(torneo, equipos, fechaInicio, hora, cancha, true);
            case GRUPOS_ELIMINATORIA -> generarGruposEliminatoria(torneo, equipos, fechaInicio, hora, cancha);
        };

        return partidoRepository.saveAll(partidos).stream()
                .map(p -> new PartidoResponse(
                        p.getId(),
                        p.getEquipoLocal().getId(), p.getEquipoLocal().getNombre(),
                        p.getEquipoVisitante().getId(), p.getEquipoVisitante().getNombre(),
                        p.getFecha(), p.getHora(), p.getCancha(),
                        p.isJugado(), p.getGolesLocal(), p.getGolesVisitante()))
                .toList();
    }

    // Liga: todos contra todos (Round Robin)
    private List<Partido> generarLiga(Torneo torneo, List<Equipo> equipos,
                                       LocalDate fechaInicio, LocalTime hora, String cancha) {
        List<Partido> partidos = new ArrayList<>();
        List<Equipo> lista = new ArrayList<>(equipos);
        // Si número impar, agregar equipo fantasma (bye)
        if (lista.size() % 2 != 0) lista.add(null);

        int n = lista.size();
        int rondas = n - 1;
        LocalDate fecha = fechaInicio;

        for (int ronda = 0; ronda < rondas; ronda++) {
            for (int i = 0; i < n / 2; i++) {
                Equipo local = lista.get(i);
                Equipo visitante = lista.get(n - 1 - i);
                if (local != null && visitante != null) {
                    partidos.add(new Partido(torneo, local, visitante, fecha, hora, cancha));
                }
            }
            fecha = fecha.plusWeeks(1);
            // Rotar equipos (el primero fijo)
            lista.add(1, lista.remove(lista.size() - 1));
        }
        return partidos;
    }

    // Eliminatoria directa (partido único o ida y vuelta)
    private List<Partido> generarEliminatoria(Torneo torneo, List<Equipo> equipos,
                                               LocalDate fechaInicio, LocalTime hora,
                                               String cancha, boolean idaYVuelta) {
        List<Partido> partidos = new ArrayList<>();
        List<Equipo> lista = new ArrayList<>(equipos);
        Collections.shuffle(lista);

        LocalDate fecha = fechaInicio;
        for (int i = 0; i + 1 < lista.size(); i += 2) {
            Equipo local = lista.get(i);
            Equipo visitante = lista.get(i + 1);
            partidos.add(new Partido(torneo, local, visitante, fecha, hora, cancha));
            if (idaYVuelta) {
                partidos.add(new Partido(torneo, visitante, local, fecha.plusWeeks(1), hora, cancha));
            }
            fecha = fecha.plusDays(idaYVuelta ? 14 : 7);
        }
        return partidos;
    }

    // Fase de grupos + eliminatoria: divide equipos en grupos de 4
    private List<Partido> generarGruposEliminatoria(Torneo torneo, List<Equipo> equipos,
                                                     LocalDate fechaInicio, LocalTime hora,
                                                     String cancha) {
        List<Partido> partidos = new ArrayList<>();
        List<Equipo> lista = new ArrayList<>(equipos);
        Collections.shuffle(lista);

        int tamanoGrupo = 4;
        int numGrupos = Math.max(1, lista.size() / tamanoGrupo);
        LocalDate fecha = fechaInicio;

        for (int g = 0; g < numGrupos; g++) {
            int inicio = g * tamanoGrupo;
            int fin = Math.min(inicio + tamanoGrupo, lista.size());
            List<Equipo> grupo = lista.subList(inicio, fin);

            // Round robin dentro del grupo
            for (int i = 0; i < grupo.size(); i++) {
                for (int j = i + 1; j < grupo.size(); j++) {
                    partidos.add(new Partido(torneo, grupo.get(i), grupo.get(j), fecha, hora, cancha));
                    fecha = fecha.plusDays(3);
                }
            }
        }
        return partidos;
    }
}
