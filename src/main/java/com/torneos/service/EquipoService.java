package com.torneos.service;

import com.torneos.dto.EquipoResponse;
import com.torneos.model.Equipo;
import com.torneos.model.Jugador;
import com.torneos.model.Torneo;
import com.torneos.repository.EquipoRepository;
import com.torneos.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
    }

    @Transactional
    public EquipoResponse registrarEquipo(Long torneoId, String nombre, List<String> nombresJugadores) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));

        String nombreNormalizado = nombre.trim();

        if (equipoRepository.existsByTorneoIdAndNombreIgnoreCase(torneoId, nombreNormalizado)) {
            throw new IllegalArgumentException("El nombre del equipo ya se encuentra registrado en este torneo.");
        }

        Equipo equipo = new Equipo(nombreNormalizado, torneo);

        if (nombresJugadores != null) {
            nombresJugadores.stream()
                    .filter(nombreJugador -> nombreJugador != null && !nombreJugador.isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(equipo::agregarJugador);
        }

        return toResponse(equipoRepository.save(equipo));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> getEquipos(Long torneoId) {
        validarTorneo(torneoId);

        return equipoRepository.findByTorneoIdOrderByNombreAsc(torneoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Equipo buscarEntidadPorId(Long torneoId, Long equipoId) {
        return equipoRepository.findByIdAndTorneoId(equipoId, torneoId)
                .orElseThrow(() -> new IllegalArgumentException("El equipo no existe en el torneo seleccionado."));
    }

    private void validarTorneo(Long torneoId) {
        if (!torneoRepository.existsById(torneoId)) {
            throw new IllegalArgumentException("El torneo no existe.");
        }
    }

    private EquipoResponse toResponse(Equipo equipo) {
        return new EquipoResponse(
                equipo.getId(),
                equipo.getNombre(),
                equipo.getJugadores().stream()
                        .map(Jugador::getNombre)
                        .toList()
        );
    }
}
