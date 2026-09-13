package com.torneos.model;

/**
 * Entidad que representa un Torneo Deportivo en el sistema.
 * Contiene la información básica para la apertura de una competencia.
 * 
 * @author Santiago Bermúdez, Carlos Hoyos G., Miguel
 * @version 1.0
 */
public class Torneo {
    private String nombre;
    private String deporte;
    private String categoria;
    private boolean activo;

    public Torneo(String nombre, String deporte, String categoria) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.activo = true; // Se inicializa activo por defecto
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDeporte() { return deporte; }
    public void setDeporte(String deporte) { this.deporte = deporte; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}