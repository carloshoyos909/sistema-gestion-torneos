package com.torneos.view;

import com.torneos.dto.EquipoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EquipoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

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

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(EquipoResponse::nombre).setHeader("Equipo").setSortable(true);

        grid.addComponentColumn(equipo -> {
            Span badge = new Span(equipo.jugadores().size() + " jugadores");
            badge.getElement().getThemeList().add("badge success");
            return badge;
        }).setHeader("Nómina");

        grid.addComponentColumn(equipo -> {
            Button btnVer = new Button("Ver detalle", new Icon(VaadinIcon.USERS));
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnVer.addClickListener(e -> abrirDetalleEquipo(equipo));
            return btnVer;
        }).setHeader("Detalle");

        grid.setWidthFull();

        torneo.addValueChangeListener(event -> {
            if (event.getValue() == null) grid.setItems();
            else grid.setItems(equipoService.getEquipos(event.getValue().id()));
        });

        add(torneo, grid);
        setPadding(true);
    }

    private void abrirDetalleEquipo(EquipoResponse equipo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("👥 " + equipo.nombre());
        dialog.setWidth("480px");

        // Encabezado con avatar del equipo
        Avatar avatarEquipo = new Avatar(equipo.nombre());
        avatarEquipo.setColorIndex(2);
        avatarEquipo.getStyle().set("width", "56px").set("height", "56px").set("font-size", "1.4rem");

        H3 nombreEquipo = new H3(equipo.nombre());
        nombreEquipo.getStyle().set("margin", "0");

        Span jugadoresCount = new Span(equipo.jugadores().size() + " jugadores registrados");
        jugadoresCount.getElement().getThemeList().add("badge success");

        VerticalLayout infoEquipo = new VerticalLayout(nombreEquipo, jugadoresCount);
        infoEquipo.setPadding(false);
        infoEquipo.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(avatarEquipo, infoEquipo);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("padding", "16px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "12px");

        VerticalLayout content = new VerticalLayout(header);
        content.setPadding(false);

        if (equipo.jugadores().isEmpty()) {
            Paragraph empty = new Paragraph("Este equipo no tiene jugadores registrados.");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("text-align", "center");
            content.add(empty);
        } else {
            // Tabla de jugadores con número de dorsal y avatar
            List<String> jugadores = equipo.jugadores();
            for (int i = 0; i < jugadores.size(); i++) {
                String nombre = jugadores.get(i);
                Avatar av = new Avatar(nombre);
                av.setColorIndex(i % 7);
                av.getStyle().set("width", "32px").set("height", "32px").set("font-size", "0.8rem");

                Span dorsal = new Span(String.valueOf(i + 1));
                dorsal.getStyle().set("font-weight", "600").set("color", "#1a237e")
                        .set("min-width", "24px").set("text-align", "center");

                Span nombreJugador = new Span(nombre);
                nombreJugador.getStyle().set("font-weight", "500");

                HorizontalLayout fila = new HorizontalLayout(dorsal, av, nombreJugador);
                fila.setAlignItems(FlexComponent.Alignment.CENTER);
                fila.setSpacing(true);
                fila.getStyle().set("padding", "6px 8px");
                if (i % 2 == 0) fila.getStyle().set("background", "var(--lumo-contrast-5pct)")
                        .set("border-radius", "6px");

                content.add(fila);
            }
        }

        content.add(new Hr());
        Button btnCerrar = new Button("Cerrar", e -> dialog.close());
        btnCerrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(btnCerrar);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(content);
        dialog.open();
    }
}
