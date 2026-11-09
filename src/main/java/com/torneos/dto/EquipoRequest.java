package com.torneos.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank(message = "El nombre del equipo no puede estar vacío")
        @Size(min = 2, max = 50, message = "El nombre del equipo debe tener entre 2 y 50 caracteres")
        String nombre,
        List<String> jugadores
) {}
