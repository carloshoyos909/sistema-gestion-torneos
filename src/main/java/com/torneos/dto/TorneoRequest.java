package com.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TorneoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 60) String deporte,
        @NotBlank @Size(max = 60) String categoria
) {
}
