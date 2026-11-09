package com.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TorneoRequest(
        @NotBlank(message = "El nombre del torneo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,
        @NotBlank(message = "El deporte es obligatorio")
        String deporte,
        @NotBlank(message = "La categoría es obligatoria")
        String categoria
) {}
