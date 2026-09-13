package com.torneos.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa un Encuentro dentro del Fixture.
 * @author Santiago Bermúdez, Carlos Hoyos G., Miguel
 */
public class Partido {
    private String equipoLocal;
    private String equipoVisitante;
    private LocalDate fecha;
    private LocalTime hora;
    private String cancha;
    
    public Partido(String equipoLocal, String equipoVisitante, LocalDate fecha, LocalTime hora, String cancha) {
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.fecha = fecha;
        this.hora = hora;
        this.cancha = cancha;
    }
    
    public String getEquipoLocal() { return equipoLocal; }
    public String getEquipoVisitante() { return equipoVisitante; }
}