package com.torneos.dto;

public record TorneoResponse(
        Long id,
        String nombre,
        String deporte,
        String categoria,
        boolean activo
) {
}
