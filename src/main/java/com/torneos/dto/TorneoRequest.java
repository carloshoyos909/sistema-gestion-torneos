package com.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TorneoRequest {

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El formato del torneo es obligatorio")
    private Long formatoId;

    // Constructor vacio
    public TorneoRequest() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getFormatoId() {
        return formatoId;
    }

    public void setFormatoId(Long formatoId) {
        this.formatoId = formatoId;
    }
}