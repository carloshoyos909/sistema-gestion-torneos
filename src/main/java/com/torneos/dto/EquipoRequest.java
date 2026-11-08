package com.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EquipoRequest {

    @NotBlank(message = "El nombre del equipo no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre del equipo debe tener entre 2 y 50 caracteres")
    private String nombre;

    public EquipoRequest() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}