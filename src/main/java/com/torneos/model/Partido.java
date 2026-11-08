package com.torneos.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 120)
    private String cancha;

    @Column(nullable = false)
    private boolean jugado = false;

    @Column(name = "goles_local")
    private Integer golesLocal;

    @Column(name = "goles_visitante")
    private Integer golesVisitante;

    // Fase del partido: "GRUPOS - Grupo A", "DIECISEISAVOS", "OCTAVOS", etc.
    @Column(length = 40)
    private String fase;

    // Número de ronda dentro de la fase (1 = primera jornada)
    @Column
    private Integer ronda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;

    protected Partido() {}

    public Partido(Torneo torneo, Equipo equipoLocal, Equipo equipoVisitante,
                   LocalDate fecha, LocalTime hora, String cancha) {
        this.torneo = torneo;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.fecha = fecha;
        this.hora = hora;
        this.cancha = cancha;
    }

    public Partido(Torneo torneo, Equipo equipoLocal, Equipo equipoVisitante,
                   LocalDate fecha, LocalTime hora, String cancha, String fase, int ronda) {
        this(torneo, equipoLocal, equipoVisitante, fecha, hora, cancha);
        this.fase = fase;
        this.ronda = ronda;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getCancha() { return cancha; }
    public boolean isJugado() { return jugado; }
    public void setJugado(boolean jugado) { this.jugado = jugado; }
    public Integer getGolesLocal() { return golesLocal; }
    public void setGolesLocal(Integer golesLocal) { this.golesLocal = golesLocal; }
    public Integer getGolesVisitante() { return golesVisitante; }
    public void setGolesVisitante(Integer golesVisitante) { this.golesVisitante = golesVisitante; }
    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }
    public Integer getRonda() { return ronda; }
    public void setRonda(Integer ronda) { this.ronda = ronda; }
    public Torneo getTorneo() { return torneo; }
    public Equipo getEquipoLocal() { return equipoLocal; }
    public Equipo getEquipoVisitante() { return equipoVisitante; }
}
