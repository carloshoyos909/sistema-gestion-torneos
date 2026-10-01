package com.torneos.view;

import com.torneos.dto.EquipoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EquipoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "equipos", layout = MainView.class)
@PageTitle("Equipos")
public class EquiposView extends VerticalLayout {

    private final EquipoService equipoService;
    private final Grid<EquipoResponse> grid = new Grid<>(EquipoResponse.class, false);

    public EquiposView(EquipoService equipoService, TorneoService torneoService) {
        this.equipoService = equipoService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidthFull();

        grid.addColumn(EquipoResponse::id).setHeader("ID");
        grid.addColumn(EquipoResponse::nombre).setHeader("Equipo");
        grid.addColumn(equipo -> String.join(", ", equipo.jugadores()))
                .setHeader("Jugadores");
        grid.setWidthFull();

        torneo.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                grid.setItems();
            } else {
                grid.setItems(equipoService.getEquipos(event.getValue().id()));
            }
        });

        add(torneo, grid);
        setPadding(true);
    }
}
