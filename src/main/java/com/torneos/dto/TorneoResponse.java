package com.torneos.dto;

import com.torneos.model.FormatoTorneo;

public record TorneoResponse(
        Long id,
        String nombre,
        String deporte,
        String categoria,
        boolean activo,
        FormatoTorneo formato
) {}
