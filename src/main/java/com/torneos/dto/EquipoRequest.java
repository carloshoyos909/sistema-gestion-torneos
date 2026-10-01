package com.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EquipoRequest(
        @NotBlank @Size(max = 100) String nombre,
        List<@NotBlank @Size(max = 120) String> jugadores
) {
}
