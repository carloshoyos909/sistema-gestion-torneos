package com.torneos.dto;

import jakarta.validation.constraints.NotNull;

public class PartidoRequest {

    @NotNull(message = "El ID del equipo local es obligatorio")
    private Long equipoLocalId;

    @NotNull(message = "El ID del equipo visitante es obligatorio")
    private Long equipoVisitanteId;

    public PartidoRequest() {}

    public Long getEquipoLocalId() {
        return equipoLocalId;
    }

    public void setEquipoLocalId(Long equipoLocalId) {
        this.equipoLocalId = equipoLocalId;
    }

    public Long getEquipoVisitanteId() {
        return equipoVisitanteId;
    }

    public void setEquipoVisitanteId(Long equipoVisitanteId) {
        this.equipoVisitanteId = equipoVisitanteId;
    }
}