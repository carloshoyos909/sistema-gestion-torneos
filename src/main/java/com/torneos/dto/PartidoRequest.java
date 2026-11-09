package com.torneos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record PartidoRequest(
        @NotNull(message = "El ID del equipo local es obligatorio")
        Long equipoLocalId,
        @NotNull(message = "El ID del equipo visitante es obligatorio")
        Long equipoVisitanteId,
        LocalDate fecha,
        LocalTime hora,
        String cancha
) {}
