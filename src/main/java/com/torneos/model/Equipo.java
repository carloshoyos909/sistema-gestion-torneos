package com.torneos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Equipo inscrito en el torneo[cite: 2].
 * @author Santiago Bermúdez, Carlos Hoyos G., Miguel
 */
public class Equipo {
    private String nombre;
    private List<String> jugadores = new ArrayList<>();

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public List<String> getJugadores() { return jugadores; }
    public void agregarJugador(String jugador) { this.jugadores.add(jugador); }
}