package com.torneos.service;

import com.torneos.dto.PartidoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Partido;
import com.torneos.model.Torneo;
import com.torneos.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    // ═══════════════════════════════════════════════════════════════════════
    // GENERAR FIXTURE INICIAL
    // ═══════════════════════════════════════════════════════════════════════
    @Transactional
    public List<PartidoResponse> generarFixture(Long torneoId, LocalDate fechaInicio,
                                                 LocalTime hora, String cancha) {
        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);
        List<Equipo> equipos = equipoService.getEntidades(torneoId);

        if (equipos.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 equipos.");
        }

        List<Partido> partidos = switch (torneo.getFormato()) {
            case LIGA ->
                    generarLiga(torneo, equipos, fechaInicio, hora, cancha);
            case ELIMINATORIA_SIMPLE ->
                    generarEliminatoriaCompleta(torneo, new ArrayList<>(equipos), fechaInicio, hora, cancha, false);
            case ELIMINATORIA_IDA_VUELTA ->
                    generarEliminatoriaCompleta(torneo, new ArrayList<>(equipos), fechaInicio, hora, cancha, true);
            case GRUPOS_ELIMINATORIA ->
                    generarSoloGrupos(torneo, equipos, fechaInicio, hora, cancha);
        };

        return partidoRepository.saveAll(partidos).stream().map(this::toResponse).toList();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GENERAR FASE ELIMINATORIA (después de que grupos estén jugados)
    // ═══════════════════════════════════════════════════════════════════════
    @Transactional
    public List<PartidoResponse> generarFaseEliminatoria(Long torneoId,
                                                          LocalDate fechaInicio,
                                                          LocalTime hora,
                                                          String cancha) {
        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);

        // Verificar que todos los partidos de grupos están jugados
        List<Partido> partidos = partidoRepository
                .findByTorneoIdAndFaseStartingWithOrderByFechaAsc(torneoId, "GRUPOS");
        boolean todosJugados = partidos.stream().allMatch(Partido::isJugado);
        if (!todosJugados) {
            throw new IllegalArgumentException(
                    "Debes registrar los resultados de TODOS los partidos de grupos antes de generar la fase eliminatoria.");
        }

        // Calcular clasificados por grupo (top 2 por puntos/DG/GF, impar: el último no clasifica)
        List<Equipo> clasificados = obtenerClasificadosPorGrupos(torneoId, partidos);

        // Si quedan impar, eliminar el último
        if (clasificados.size() % 2 != 0) {
            clasificados.remove(clasificados.size() - 1);
        }

        if (clasificados.size() < 2) {
            throw new IllegalArgumentException("No hay suficientes clasificados para la fase eliminatoria.");
        }

        // Generar la eliminatoria con los clasificados reales
        List<Partido> eliminatoria = generarEliminatoriaCompleta(
                torneo, clasificados, fechaInicio, hora, cancha, false);

        return partidoRepository.saveAll(eliminatoria).stream().map(this::toResponse).toList();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VERIFICAR SI SE PUEDE GENERAR LA FASE ELIMINATORIA
    // ═══════════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public boolean puedeGenerarEliminatoria(Long torneoId) {
        // Solo aplica para formato GRUPOS_ELIMINATORIA
        boolean tieneGrupos = partidoRepository.existsByTorneoIdAndFaseStartingWith(torneoId, "GRUPOS");
        if (!tieneGrupos) return false;

        // No debe tener ya fase eliminatoria generada
        boolean yaHayEliminatoria = partidoRepository.existsByTorneoIdAndFaseStartingWith(torneoId, "SEMIFINAL")
                || partidoRepository.existsByTorneoIdAndFaseStartingWith(torneoId, "CUARTOS")
                || partidoRepository.existsByTorneoIdAndFaseStartingWith(torneoId, "FINAL")
                || partidoRepository.existsByTorneoIdAndFaseStartingWith(torneoId, "OCTAVOS");
        if (yaHayEliminatoria) return false;

        // Todos los partidos de grupos deben estar jugados
        List<Partido> gruposPartidos = partidoRepository
                .findByTorneoIdAndFaseStartingWithOrderByFechaAsc(torneoId, "GRUPOS");
        return !gruposPartidos.isEmpty() && gruposPartidos.stream().allMatch(Partido::isJugado);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LIGA: Round Robin
    // ═══════════════════════════════════════════════════════════════════════
    private List<Partido> generarLiga(Torneo torneo, List<Equipo> equipos,
                                       LocalDate fechaInicio, LocalTime hora, String cancha) {
        List<Partido> partidos = new ArrayList<>();
        List<Equipo> lista = new ArrayList<>(equipos);
        if (lista.size() % 2 != 0) lista.add(null);

        int n = lista.size();
        LocalDate fecha = fechaInicio;

        for (int ronda = 1; ronda <= n - 1; ronda++) {
            for (int i = 0; i < n / 2; i++) {
                Equipo local = lista.get(i);
                Equipo visitante = lista.get(n - 1 - i);
                if (local != null && visitante != null) {
                    partidos.add(new Partido(torneo, local, visitante, fecha, hora, cancha, "LIGA", ronda));
                }
            }
            fecha = fecha.plusWeeks(1);
            lista.add(1, lista.remove(lista.size() - 1));
        }
        return partidos;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SOLO GRUPOS (sin eliminatoria — espera resultados reales)
    // Reglas:
    //   < 5 equipos → 1 grupo
    //   5 o 6 equipos → 2 grupos
    //   7+ → grupos de 4
    // ═══════════════════════════════════════════════════════════════════════
    private List<Partido> generarSoloGrupos(Torneo torneo, List<Equipo> equipos,
                                             LocalDate fechaInicio, LocalTime hora, String cancha) {
        List<Partido> partidos = new ArrayList<>();
        List<Equipo> lista = new ArrayList<>(equipos);
        Collections.shuffle(lista);

        // Determinar número de grupos según cantidad de equipos
        int numGrupos;
        if (lista.size() < 5) {
            numGrupos = 1;
        } else if (lista.size() <= 6) {
            numGrupos = 2;
        } else {
            numGrupos = (int) Math.ceil(lista.size() / 4.0);
        }

        int tamanoGrupo = (int) Math.ceil((double) lista.size() / numGrupos);
        LocalDate fecha = fechaInicio;
        char letraGrupo = 'A';

        for (int g = 0; g < numGrupos; g++) {
            int inicio = g * tamanoGrupo;
            int fin = Math.min(inicio + tamanoGrupo, lista.size());
            List<Equipo> grupo = new ArrayList<>(lista.subList(inicio, fin));
            if (grupo.size() < 2) continue;

            String nombreFase = "GRUPOS - Grupo " + letraGrupo;
            List<Equipo> listaGrupo = new ArrayList<>(grupo);
            if (listaGrupo.size() % 2 != 0) listaGrupo.add(null);
            int n = listaGrupo.size();
            int jornada = 1;

            for (int ronda = 0; ronda < n - 1; ronda++) {
                for (int i = 0; i < n / 2; i++) {
                    Equipo local = listaGrupo.get(i);
                    Equipo visitante = listaGrupo.get(n - 1 - i);
                    if (local != null && visitante != null) {
                        partidos.add(new Partido(torneo, local, visitante,
                                fecha, hora, cancha, nombreFase, jornada));
                    }
                }
                fecha = fecha.plusDays(4);
                jornada++;
                listaGrupo.add(1, listaGrupo.remove(listaGrupo.size() - 1));
            }

            letraGrupo++;
            fecha = fecha.plusWeeks(1);
        }

        return partidos;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ELIMINATORIA COMPLETA por fases (con nombres correctos)
    // 4 clasificados → SEMIFINAL (no cuartos)
    // ═══════════════════════════════════════════════════════════════════════
    private List<Partido> generarEliminatoriaCompleta(Torneo torneo, List<Equipo> equipos,
                                                       LocalDate fechaInicio, LocalTime hora,
                                                       String cancha, boolean idaYVuelta) {
        List<Partido> todos = new ArrayList<>();
        List<Equipo> ronda = new ArrayList<>(equipos);
        Collections.shuffle(ronda);

        // Quitar impares (no pasan)
        if (ronda.size() % 2 != 0) ronda.remove(ronda.size() - 1);

        LocalDate fecha = fechaInicio;
        int rondaNum = 1;

        while (ronda.size() >= 2) {
            String fase = determinarFase(ronda.size(), idaYVuelta);
            List<Equipo> siguienteRonda = new ArrayList<>();

            for (int i = 0; i + 1 < ronda.size(); i += 2) {
                Equipo local = ronda.get(i);
                Equipo visitante = ronda.get(i + 1);
                todos.add(new Partido(torneo, local, visitante, fecha, hora, cancha, fase, rondaNum));
                if (idaYVuelta) {
                    todos.add(new Partido(torneo, visitante, local,
                            fecha.plusWeeks(1), hora, cancha, fase + " (vuelta)", rondaNum));
                }
                siguienteRonda.add(null); // ganador real se asigna tras resultados
            }

            fecha = fecha.plusWeeks(idaYVuelta ? 3 : 2);
            ronda = siguienteRonda;
            rondaNum++;

            // Solo generar la primera ronda — las siguientes requieren resultados reales
            break;
        }

        return todos;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBTENER CLASIFICADOS POR GRUPO (top 2 por PTS → DG → GF)
    // ═══════════════════════════════════════════════════════════════════════
    private List<Equipo> obtenerClasificadosPorGrupos(Long torneoId, List<Partido> partidos) {
        // Agrupar partidos por grupo
        Map<String, List<Partido>> porGrupo = partidos.stream()
                .collect(Collectors.groupingBy(p -> p.getFase() != null ? p.getFase() : ""));

        List<Equipo> clasificados = new ArrayList<>();

        // Ordenar grupos alfabéticamente para consistencia
        List<String> gruposOrdenados = new ArrayList<>(porGrupo.keySet());
        Collections.sort(gruposOrdenados);

        for (String grupo : gruposOrdenados) {
            List<Partido> partidosGrupo = porGrupo.get(grupo);
            Map<Long, EquipoStats> stats = new LinkedHashMap<>();

            for (Partido p : partidosGrupo) {
                if (!p.isJugado()) continue;
                Equipo local = p.getEquipoLocal();
                Equipo visitante = p.getEquipoVisitante();
                stats.putIfAbsent(local.getId(), new EquipoStats(local));
                stats.putIfAbsent(visitante.getId(), new EquipoStats(visitante));
                stats.get(local.getId()).registrar(p.getGolesLocal(), p.getGolesVisitante());
                stats.get(visitante.getId()).registrar(p.getGolesVisitante(), p.getGolesLocal());
            }

            // Ordenar por PTS → DG → GF, tomar top 2
            List<EquipoStats> ranking = new ArrayList<>(stats.values());
            ranking.sort(Comparator
                    .comparingInt(EquipoStats::getPuntos)
                    .thenComparingInt(EquipoStats::getDg)
                    .thenComparingInt(EquipoStats::getGf)
                    .reversed());

            int clasificadosPorGrupo = Math.min(2, ranking.size());
            for (int i = 0; i < clasificadosPorGrupo; i++) {
                clasificados.add(ranking.get(i).equipo);
            }
        }

        return clasificados;
    }

    // ─── Determinar nombre de fase según número de equipos ──────────────────
    // 4 equipos → SEMIFINAL (no CUARTOS)
    private String determinarFase(int numEquipos, boolean idaYVuelta) {
        String sufijo = idaYVuelta ? " (ida)" : "";
        if (numEquipos >= 16) return "DIECISEISAVOS" + sufijo;
        if (numEquipos >= 8)  return "OCTAVOS" + sufijo;
        if (numEquipos >= 6)  return "CUARTOS DE FINAL" + sufijo;
        if (numEquipos >= 4)  return "SEMIFINAL" + sufijo;
        return "FINAL" + sufijo;
    }

    private PartidoResponse toResponse(Partido p) {
        return new PartidoResponse(
                p.getId(),
                p.getEquipoLocal().getId(), p.getEquipoLocal().getNombre(),
                p.getEquipoVisitante().getId(), p.getEquipoVisitante().getNombre(),
                p.getFecha(), p.getHora(), p.getCancha(),
                p.isJugado(), p.getGolesLocal(), p.getGolesVisitante(),
                p.getFase(), p.getRonda()
        );
    }

    // ─── Clase auxiliar para calcular estadísticas de grupo ─────────────────
    private static class EquipoStats {
        final Equipo equipo;
        int puntos, gf, gc;

        EquipoStats(Equipo e) { this.equipo = e; }

        void registrar(int golesAFavor, int golesEnContra) {
            gf += golesAFavor;
            gc += golesEnContra;
            if (golesAFavor > golesEnContra) puntos += 3;
            else if (golesAFavor == golesEnContra) puntos += 1;
        }

        int getPuntos() { return puntos; }
        int getDg() { return gf - gc; }
        int getGf() { return gf; }
    }
}
