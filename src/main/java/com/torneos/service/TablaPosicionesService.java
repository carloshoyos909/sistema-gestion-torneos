package com.torneos.service;

import com.torneos.dto.TablaPosicionesDTO;
import com.torneos.model.Equipo;
import com.torneos.model.Partido;
import com.torneos.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TablaPosicionesService {

    private final PartidoRepository partidoRepository;
    private final TorneoService torneoService;
    private final EquipoService equipoService;

    public TablaPosicionesService(PartidoRepository partidoRepository,
                                   TorneoService torneoService,
                                   EquipoService equipoService) {
        this.partidoRepository = partidoRepository;
        this.torneoService = torneoService;
        this.equipoService = equipoService;
    }

    @Transactional(readOnly = true)
    public List<TablaPosicionesDTO> obtenerTablaOrdenada(Long torneoId) {
        torneoService.buscarEntidadPorId(torneoId);

        // Primero registrar TODOS los equipos del torneo con 0 puntos
        Map<String, TablaPosicionesDTO> mapa = new LinkedHashMap<>();
        for (Equipo equipo : equipoService.getEntidades(torneoId)) {
            mapa.put(equipo.getNombre(), new TablaPosicionesDTO(equipo.getNombre()));
        }

        // Luego procesar solo los partidos jugados
        List<Partido> partidos = partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId);
        for (Partido p : partidos) {
            if (p.isJugado() && p.getGolesLocal() != null && p.getGolesVisitante() != null) {
                String local = p.getEquipoLocal().getNombre();
                String visitante = p.getEquipoVisitante().getNombre();
                mapa.putIfAbsent(local, new TablaPosicionesDTO(local));
                mapa.putIfAbsent(visitante, new TablaPosicionesDTO(visitante));
                mapa.get(local).registrarPartido(p.getGolesLocal(), p.getGolesVisitante());
                mapa.get(visitante).registrarPartido(p.getGolesVisitante(), p.getGolesLocal());
            }
        }

        if (mapa.isEmpty()) return new ArrayList<>();

        // Ordenar: PTS desc → DG desc → GF desc
        List<TablaPosicionesDTO> tabla = new ArrayList<>(mapa.values());
        tabla.sort(Comparator
                .comparingInt(TablaPosicionesDTO::getPuntos)
                .thenComparingInt(TablaPosicionesDTO::getDiferenciaGoles)
                .thenComparingInt(TablaPosicionesDTO::getGolesFavor)
                .reversed());

        for (int i = 0; i < tabla.size(); i++) {
            tabla.get(i).setPosicion(i + 1);
        }

        return tabla;
    }
}
