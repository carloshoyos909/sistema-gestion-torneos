package com.torneos.view;

import com.torneos.dto.EquipoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EquipoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "equipos", layout = MainView.class)
@PageTitle("Equipos")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class EquiposView extends VerticalLayout {

    private final EquipoService equipoService;
    private final Grid<EquipoResponse> grid = new Grid<>(EquipoResponse.class, false);

    public EquiposView(EquipoService equipoService, TorneoService torneoService) {
        this.equipoService = equipoService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidthFull();

        grid.addColumn(EquipoResponse::nombre).setHeader("Equipo").setSortable(true);
        grid.addColumn(equipo -> equipo.jugadores().size() + " jugadores").setHeader("Nómina");

        grid.addComponentColumn(equipo -> {
            Button btnVer = new Button("Ver detalle");
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnVer.addClickListener(e -> abrirDetalleEquipo(equipo));
            return btnVer;
        }).setHeader("Detalle");

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

    private void abrirDetalleEquipo(EquipoResponse equipo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("👥 " + equipo.nombre());
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        H3 tituloNomina = new H3("Nómina de jugadores");
        content.add(tituloNomina);

        if (equipo.jugadores().isEmpty()) {
            content.add(new Span("Este equipo no tiene jugadores registrados."));
        } else {
            Grid<String> gridJugadores = new Grid<>();
            gridJugadores.addColumn(nombre -> nombre).setHeader("Jugador");
            gridJugadores.setItems(equipo.jugadores());
            gridJugadores.setAllRowsVisible(true);
            gridJugadores.setWidthFull();
            content.add(gridJugadores);
        }

        Button btnCerrar = new Button("Cerrar", e -> dialog.close());
        btnCerrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content, btnCerrar);
        dialog.open();
    }
}
