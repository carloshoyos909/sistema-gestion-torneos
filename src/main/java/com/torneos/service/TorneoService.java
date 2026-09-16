package com.torneos.service;

import com.torneos.model.Torneo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TorneoService {

    private final List<Torneo> torneos = new ArrayList<>();

    public Torneo crearTorneo(String nombre, String deporte, String categoria) {
        Torneo torneo = new Torneo(nombre, deporte, categoria);
        torneos.add(torneo);
        return torneo;
    }

    public List<Torneo> getTorneos() {
        return new ArrayList<>(torneos);
    }

    public Optional<Torneo> buscarPorNombre(String nombre) {
        return torneos.stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public List<Torneo> getTorneosActivos() {
        return torneos.stream()
                .filter(Torneo::isActivo)
                .toList();
    }
}
