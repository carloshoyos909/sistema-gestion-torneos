package com.torneos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.torneos.dto.TorneoResponse;
import com.torneos.model.FormatoTorneo;
import com.torneos.model.Torneo;
import com.torneos.repository.TorneoRepository;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;

    public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

    @Transactional
    public TorneoResponse crearTorneo(String nombre, String deporte, String categoria, FormatoTorneo formato) {
        String nombreNormalizado = nombre.trim();

        if (torneoRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("El nombre del torneo ya se encuentra registrado.");
        }

        Torneo torneo = new Torneo(nombreNormalizado, deporte.trim(), categoria.trim(), formato);
        return toResponse(torneoRepository.save(torneo));
    }

    @Transactional
    public TorneoResponse crearTorneo(String nombre, String deporte, String categoria) {
        return crearTorneo(nombre, deporte, categoria, FormatoTorneo.LIGA);
    }

    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneos() {
        return torneoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Torneo buscarEntidadPorId(Long id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El torneo no existe."));
    }

    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneosActivos() {
        return torneoRepository.findAll().stream()
                .filter(Torneo::isActivo)
                .map(this::toResponse)
                .toList();
    }

    private TorneoResponse toResponse(Torneo torneo) {
        return new TorneoResponse(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getDeporte(),
                torneo.getCategoria(),
                torneo.isActivo(),
                torneo.getFormato()
        );
    }
}
