package com.torneos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record PartidoRequest(
        @NotNull Long equipoLocalId,
        @NotNull Long equipoVisitanteId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime hora,
        @NotBlank @Size(max = 120) String cancha
) {
}
