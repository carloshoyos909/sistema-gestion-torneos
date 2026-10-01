package com.torneos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PartidoResponse(
        Long id,
        Long equipoLocalId,
        String equipoLocal,
        Long equipoVisitanteId,
        String equipoVisitante,
        LocalDate fecha,
        LocalTime hora,
        String cancha
) {
}
