package com.torneos.model;

public enum FormatoTorneo {
    LIGA("Liga — Todos contra todos"),
    ELIMINATORIA_SIMPLE("Eliminatoria Directa — Partido Único"),
    ELIMINATORIA_IDA_VUELTA("Eliminatoria Directa — Ida y Vuelta"),
    GRUPOS_ELIMINATORIA("Fase de Grupos + Eliminatoria");

    private final String label;

    FormatoTorneo(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
