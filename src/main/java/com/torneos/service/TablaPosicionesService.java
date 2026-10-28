package com.torneos.service;

import com.torneos.dto.TablaPosicionesDTO;
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

    public TablaPosicionesService(PartidoRepository partidoRepository,
                                   TorneoService torneoService) {
        this.partidoRepository = partidoRepository;
        this.torneoService = torneoService;
    }

    @Transactional(readOnly = true)
    public List<TablaPosicionesDTO> obtenerTablaOrdenada(Long torneoId) {
        torneoService.buscarEntidadPorId(torneoId);

        List<Partido> partidos = partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId);

        // Construir mapa equipo -> estadísticas
        Map<String, TablaPosicionesDTO> mapa = new LinkedHashMap<>();

        for (Partido p : partidos) {
            String local = p.getEquipoLocal().getNombre();
            String visitante = p.getEquipoVisitante().getNombre();

            mapa.putIfAbsent(local, new TablaPosicionesDTO(local));
            mapa.putIfAbsent(visitante, new TablaPosicionesDTO(visitante));

            if (p.isJugado() && p.getGolesLocal() != null && p.getGolesVisitante() != null) {
                mapa.get(local).registrarPartido(p.getGolesLocal(), p.getGolesVisitante());
                mapa.get(visitante).registrarPartido(p.getGolesVisitante(), p.getGolesLocal());
            }
        }

        // Ordenar: PTS desc → DG desc → GF desc
        List<TablaPosicionesDTO> tabla = new ArrayList<>(mapa.values());
        tabla.sort(Comparator
                .comparingInt(TablaPosicionesDTO::getPuntos)
                .thenComparingInt(TablaPosicionesDTO::getDiferenciaGoles)
                .thenComparingInt(TablaPosicionesDTO::getGolesFavor)
                .reversed());

        // Asignar posición
        for (int i = 0; i < tabla.size(); i++) {
            tabla.get(i).setPosicion(i + 1);
        }

        return tabla;
    }
}
