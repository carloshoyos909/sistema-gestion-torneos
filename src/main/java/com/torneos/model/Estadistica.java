package com.torneos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estadisticas")
public class Estadistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(nullable = false)
    private int goles = 0;

    @Column(nullable = false)
    private int asistencias = 0;

    @Column(name = "partidos_jugados", nullable = false)
    private int partidosJugados = 0;

    @Column(name = "tarjetas_amarillas", nullable = false)
    private int tarjetasAmarillas = 0;

    @Column(name = "tarjetas_rojas", nullable = false)
    private int tarjetasRojas = 0;

    protected Estadistica() {}

    public Estadistica(Jugador jugador, Torneo torneo) {
        this.jugador = jugador;
        this.torneo = torneo;
    }

    public Long getId() { return id; }
    public Jugador getJugador() { return jugador; }
    public Torneo getTorneo() { return torneo; }
    public int getGoles() { return goles; }
    public void setGoles(int goles) { this.goles = goles; }
    public int getAsistencias() { return asistencias; }
    public void setAsistencias(int asistencias) { this.asistencias = asistencias; }
    public int getPartidosJugados() { return partidosJugados; }
    public void setPartidosJugados(int partidosJugados) { this.partidosJugados = partidosJugados; }
    public int getTarjetasAmarillas() { return tarjetasAmarillas; }
    public void setTarjetasAmarillas(int tarjetasAmarillas) { this.tarjetasAmarillas = tarjetasAmarillas; }
    public int getTarjetasRojas() { return tarjetasRojas; }
    public void setTarjetasRojas(int tarjetasRojas) { this.tarjetasRojas = tarjetasRojas; }
}
