package com.torneos.dto;

import java.util.List;

public record EquipoResponse(
        Long id,
        String nombre,
        List<String> jugadores
) {
}
