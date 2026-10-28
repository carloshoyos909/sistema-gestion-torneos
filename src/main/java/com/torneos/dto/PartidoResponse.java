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
        String cancha,
        boolean jugado,
        Integer golesLocal,
        Integer golesVisitante
) {
    public String estado() {
        if (jugado && golesLocal != null && golesVisitante != null) {
            return golesLocal + " - " + golesVisitante;
        }
        return java.time.LocalDate.now().isAfter(fecha) ? "Pendiente" : "Próximo";
    }
}
