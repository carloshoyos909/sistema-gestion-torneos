package com.torneos.view;

import com.torneos.dto.EstadisticaResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EstadisticaService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "jugador", layout = MainView.class)
@PageTitle("Panel Jugador")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class JugadorView extends VerticalLayout {

    private final EstadisticaService estadisticaService;
    private final Grid<EstadisticaResponse> grid = new Grid<>(EstadisticaResponse.class, false);
    private TorneoResponse torneoSeleccionado;

    public JugadorView(EstadisticaService estadisticaService, TorneoService torneoService) {
        this.estadisticaService = estadisticaService;

        H2 titulo = new H2("⚽ Panel de Jugador — Estadísticas");

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("300px");

        TextField buscarJugador = new TextField();
        buscarJugador.setPlaceholder("Buscar por nombre de jugador...");
        buscarJugador.setWidth("300px");
        buscarJugador.setClearButtonVisible(true);

        HorizontalLayout filtros = new HorizontalLayout(torneo, buscarJugador);
        filtros.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        configurarGrid();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            actualizarGrid(buscarJugador.getValue());
        });

        buscarJugador.addValueChangeListener(event -> actualizarGrid(event.getValue()));

        add(titulo, filtros, grid);
        setPadding(true);
        setSpacing(true);
    }

    private void configurarGrid() {
        grid.addColumn(EstadisticaResponse::jugador).setHeader("Jugador").setSortable(true);
        grid.addColumn(EstadisticaResponse::equipo).setHeader("Equipo").setSortable(true);
        grid.addColumn(EstadisticaResponse::goles).setHeader("Goles").setSortable(true);
        grid.addColumn(EstadisticaResponse::asistencias).setHeader("Asistencias").setSortable(true);
        grid.addColumn(EstadisticaResponse::partidosJugados).setHeader("Partidos Jugados");
        grid.addColumn(EstadisticaResponse::tarjetasAmarillas).setHeader("🟡 Amarillas").setSortable(true);
        grid.addColumn(EstadisticaResponse::tarjetasRojas).setHeader("🔴 Rojas").setSortable(true);
        grid.setWidthFull();
    }

    private void actualizarGrid(String filtroNombre) {
        if (torneoSeleccionado == null) {
            grid.setItems();
            return;
        }
        grid.setItems(estadisticaService.getEstadisticas(torneoSeleccionado.id(), filtroNombre));
    }
}
