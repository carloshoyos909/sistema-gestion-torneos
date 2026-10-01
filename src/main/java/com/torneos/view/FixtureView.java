package com.torneos.view;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.PartidoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "fixture", layout = MainView.class)
@PageTitle("Fixture")
public class FixtureView extends VerticalLayout {

    private final PartidoService partidoService;
    private final Grid<PartidoResponse> grid = new Grid<>(PartidoResponse.class, false);

    public FixtureView(PartidoService partidoService, TorneoService torneoService) {
        this.partidoService = partidoService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidthFull();

        grid.addColumn(PartidoResponse::fecha).setHeader("Fecha");
        grid.addColumn(PartidoResponse::hora).setHeader("Hora");
        grid.addColumn(PartidoResponse::equipoLocal).setHeader("Local");
        grid.addColumn(PartidoResponse::equipoVisitante).setHeader("Visitante");
        grid.addColumn(PartidoResponse::cancha).setHeader("Cancha");
        grid.setWidthFull();

        torneo.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                grid.setItems();
            } else {
                grid.setItems(partidoService.getPartidos(event.getValue().id()));
            }
        });

        add(torneo, grid);
        setPadding(true);
    }
}
