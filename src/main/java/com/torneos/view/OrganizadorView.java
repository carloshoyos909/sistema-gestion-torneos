package com.torneos.view;

import java.util.List;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.FixtureGeneratorService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "organizador", layout = MainView.class)
@PageTitle("Panel Organizador")
@RolesAllowed({"ADMIN", "ORGANIZADOR"})
public class OrganizadorView extends VerticalLayout {

    private final FixtureGeneratorService fixtureGeneratorService;
    private final TorneoService torneoService;
    private final Grid<PartidoResponse> gridFixture = new Grid<>(PartidoResponse.class, false);

    public OrganizadorView(FixtureGeneratorService fixtureGeneratorService,
                           TorneoService torneoService) {
        this.fixtureGeneratorService = fixtureGeneratorService;
        this.torneoService = torneoService;

        H2 titulo = new H2("📋 Panel de Organizador");

        H3 tituloGenerador = new H3("Generador Automático de Fixture");

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(t -> t.nombre() + " — " + t.formato().getLabel());
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("400px");

        DatePicker fechaInicio = new DatePicker("Fecha de inicio");
        fechaInicio.setRequired(true);

        TimePicker hora = new TimePicker("Hora de los partidos");
        hora.setRequired(true);

        TextField cancha = new TextField("Cancha / Lugar");
        cancha.setRequired(true);
        cancha.setWidth("250px");

        Button btnGenerar = new Button("⚡ Generar Fixture", e -> {
            if (torneo.getValue() == null || fechaInicio.getValue() == null
                    || hora.getValue() == null || cancha.isEmpty()) {
                mostrarError("Completa todos los campos antes de generar el fixture.");
                return;
            }
            try {
                List<PartidoResponse> partidos = fixtureGeneratorService.generarFixture(
                        torneo.getValue().id(),
                        fechaInicio.getValue(),
                        hora.getValue(),
                        cancha.getValue()
                );
                gridFixture.setItems(partidos);
                mostrarExito("Fixture generado con " + partidos.size() + " partido(s) para el formato: "
                        + torneo.getValue().formato().getLabel());
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnGenerar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout campos = new HorizontalLayout(fechaInicio, hora, cancha);
        campos.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        configurarGridFixture();

        add(titulo, tituloGenerador, torneo, campos, btnGenerar, gridFixture);
        setPadding(true);
        setSpacing(true);
    }

    private void configurarGridFixture() {
        gridFixture.addColumn(PartidoResponse::fecha).setHeader("Fecha");
        gridFixture.addColumn(PartidoResponse::hora).setHeader("Hora");
        gridFixture.addColumn(PartidoResponse::equipoLocal).setHeader("Local");
        gridFixture.addColumn(PartidoResponse::equipoVisitante).setHeader("Visitante");
        gridFixture.addColumn(PartidoResponse::cancha).setHeader("Cancha");
        gridFixture.setWidthFull();
        gridFixture.setVisible(true);
    }

    private void mostrarExito(String mensaje) {
        Notification n = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
