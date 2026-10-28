package com.torneos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FormatoTorneo formato = FormatoTorneo.LIGA;

    protected Torneo() {}

    public Torneo(String nombre, String deporte, String categoria) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.formato = FormatoTorneo.LIGA;
        this.activo = true;
    }

    public Torneo(String nombre, String deporte, String categoria, FormatoTorneo formato) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.formato = formato;
        this.activo = true;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDeporte() { return deporte; }
    public String getCategoria() { return categoria; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public FormatoTorneo getFormato() { return formato; }
    public void setFormato(FormatoTorneo formato) { this.formato = formato; }
}
