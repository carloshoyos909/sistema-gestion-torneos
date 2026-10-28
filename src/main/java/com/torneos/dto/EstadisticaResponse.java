package com.torneos.dto;

public record EstadisticaResponse(
        Long id,
        Long jugadorId,
        String jugador,
        String equipo,
        int goles,
        int asistencias,
        int partidosJugados,
        int tarjetasAmarillas,
        int tarjetasRojas
) {}
