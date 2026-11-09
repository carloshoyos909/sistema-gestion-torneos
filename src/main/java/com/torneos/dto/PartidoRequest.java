package com.torneos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record PartidoRequest(
        @NotNull(message = "El ID del equipo local es obligatorio")
        Long equipoLocalId,
        @NotNull(message = "El ID del equipo visitante es obligatorio")
        Long equipoVisitanteId,
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        @NotNull(message = "La hora es obligatoria")
        LocalTime hora,
        @NotNull(message = "La cancha es obligatoria")
        String cancha
) {}
