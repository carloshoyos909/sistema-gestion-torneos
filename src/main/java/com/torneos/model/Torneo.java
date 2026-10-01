package com.torneos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "torneos")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String deporte;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false)
    private boolean activo = true;

    protected Torneo() {
    }

    public Torneo(String nombre, String deporte, String categoria) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.activo = true;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDeporte() { return deporte; }
    public String getCategoria() { return categoria; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
