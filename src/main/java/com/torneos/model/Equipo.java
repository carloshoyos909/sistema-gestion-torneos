package com.torneos.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores = new ArrayList<>();

    protected Equipo() {
    }

    public Equipo(String nombre, Torneo torneo) {
        this.nombre = nombre;
        this.torneo = torneo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Torneo getTorneo() { return torneo; }
    public List<Jugador> getJugadores() { return jugadores; }

    public void agregarJugador(String nombreJugador) {
        jugadores.add(new Jugador(nombreJugador, this));
    }
}
