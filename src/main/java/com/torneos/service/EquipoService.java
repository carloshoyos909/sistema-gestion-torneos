package com.torneos.service;

import com.torneos.model.Equipo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    private final List<Equipo> equipos = new ArrayList<>();

    public Equipo registrarEquipo(String nombre) {
        if (existeEquipo(nombre)) {
            throw new IllegalArgumentException("El nombre del equipo ya se encuentra registrado.");
        }
        Equipo equipo = new Equipo(nombre);
        equipos.add(equipo);
        return equipo;
    }

    public boolean existeEquipo(String nombre) {
        return equipos.stream()
                .anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre));
    }

    public List<Equipo> getEquipos() {
        return new ArrayList<>(equipos);
    }

    public List<String> getNombresEquipos() {
        return equipos.stream()
                .map(Equipo::getNombre)
                .toList();
    }

    public Optional<Equipo> buscarPorNombre(String nombre) {
        return equipos.stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }
}
