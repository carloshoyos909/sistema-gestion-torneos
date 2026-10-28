package com.torneos.dto;

public class TablaPosicionesDTO {

    private int posicion;
    private String equipo;
    private int partidosJugados;
    private int partidosGanados;
    private int partidosEmpatados;
    private int partidosPerdidos;
    private int golesFavor;
    private int golesContra;
    private int diferenciaGoles;
    private int puntos;

    public TablaPosicionesDTO(String equipo) {
        this.equipo = equipo;
    }

    public void registrarPartido(int golesAFavor, int golesEnContra) {
        this.partidosJugados++;
        this.golesFavor += golesAFavor;
        this.golesContra += golesEnContra;
        this.diferenciaGoles = this.golesFavor - this.golesContra;

        if (golesAFavor > golesEnContra) {
            this.partidosGanados++;
            this.puntos += 3;
        } else if (golesAFavor == golesEnContra) {
            this.partidosEmpatados++;
            this.puntos += 1;
        } else {
            this.partidosPerdidos++;
        }
    }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
    public String getEquipo() { return equipo; }
    public int getPartidosJugados() { return partidosJugados; }
    public int getPartidosGanados() { return partidosGanados; }
    public int getPartidosEmpatados() { return partidosEmpatados; }
    public int getPartidosPerdidos() { return partidosPerdidos; }
    public int getGolesFavor() { return golesFavor; }
    public int getGolesContra() { return golesContra; }
    public int getDiferenciaGoles() { return diferenciaGoles; }
    public int getPuntos() { return puntos; }
}
