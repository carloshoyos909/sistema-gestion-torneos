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
import java.util.List;

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
    public PartidoResponse programarPartido(Long torneoId,
                                            Long equipoLocalId,
                                            Long equipoVisitanteId,
                                            LocalDate fecha,
                                            LocalTime hora,
                                            String cancha) {
        if (equipoLocalId.equals(equipoVisitanteId)) {
            throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo.");
        }

        Torneo torneo = torneoService.buscarEntidadPorId(torneoId);
        Equipo equipoLocal = equipoService.buscarEntidadPorId(torneoId, equipoLocalId);
        Equipo equipoVisitante = equipoService.buscarEntidadPorId(torneoId, equipoVisitanteId);

        Partido partido = new Partido(
                torneo,
                equipoLocal,
                equipoVisitante,
                fecha,
                hora,
                cancha.trim()
        );

        return toResponse(partidoRepository.save(partido));
    }

    @Transactional(readOnly = true)
    public List<PartidoResponse> getPartidos(Long torneoId) {
        torneoService.buscarEntidadPorId(torneoId);

        return partidoRepository.findByTorneoIdOrderByFechaAscHoraAsc(torneoId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PartidoResponse toResponse(Partido partido) {
        return new PartidoResponse(
                partido.getId(),
                partido.getEquipoLocal().getId(),
                partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getId(),
                partido.getEquipoVisitante().getNombre(),
                partido.getFecha(),
                partido.getHora(),
                partido.getCancha()
        );
    }
}
