package com.torneos.view;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.FixtureGeneratorService;
import com.torneos.service.PartidoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "organizador", layout = MainView.class)
@PageTitle("Panel Organizador")
@RolesAllowed({"ADMIN", "ORGANIZADOR"})
public class OrganizadorView extends VerticalLayout {

    private final FixtureGeneratorService fixtureGeneratorService;
    private final TorneoService torneoService;
    private final PartidoService partidoService;
    private final VerticalLayout contenedorFases = new VerticalLayout();
    private final Button btnElim = new Button("🏆 Generar Fase Eliminatoria");
    private TorneoResponse torneoActual;

    public OrganizadorView(FixtureGeneratorService fixtureGeneratorService,
                           TorneoService torneoService,
                           PartidoService partidoService) {
        this.fixtureGeneratorService = fixtureGeneratorService;
        this.torneoService = torneoService;
        this.partidoService = partidoService;

        H2 titulo = new H2("📋 Panel de Organizador");
        H3 tituloGenerador = new H3("Generador Automático de Fixture");

        // Mostrar TODOS los torneos
        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(t -> t.nombre() + " — " + t.formato().getLabel());
        torneo.setItems(torneoService.getTorneosParaGestion());
        torneo.setWidth("450px");

        DatePicker fechaInicio = new DatePicker("Fecha de inicio");
        fechaInicio.setRequired(true);

        TimePicker hora = new TimePicker("Hora de los partidos");
        hora.setRequired(true);

        TextField cancha = new TextField("Cancha / Lugar");
        cancha.setRequired(true);
        cancha.setWidth("250px");

        // Botón secundario: genera fase eliminatoria (solo cuando grupos terminaron)
        btnElim.addClickListener(e -> {
            if (torneo.getValue() == null || fechaInicio.getValue() == null
                    || hora.getValue() == null || cancha.isEmpty()) {
                mostrarError("Completa fecha, hora y cancha.");
                return;
            }
            try {
                fixtureGeneratorService.generarFaseEliminatoria(
                        torneo.getValue().id(),
                        fechaInicio.getValue(),
                        hora.getValue(),
                        cancha.getValue());
                recargarFixture(torneo.getValue());
                btnElim.setVisible(false);
                mostrarExito("Fase eliminatoria generada.");
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnElim.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnElim.setVisible(false);

        // Botón principal: genera fixture inicial
        Button btnGenerar = new Button("⚡ Generar Fixture", e -> {
            if (torneo.getValue() == null || fechaInicio.getValue() == null
                    || hora.getValue() == null || cancha.isEmpty()) {
                mostrarError("Completa todos los campos.");
                return;
            }
            try {
                fixtureGeneratorService.generarFixture(
                        torneo.getValue().id(),
                        fechaInicio.getValue(),
                        hora.getValue(),
                        cancha.getValue());
                recargarFixture(torneo.getValue());
                mostrarExito("Fixture generado correctamente.");
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnGenerar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        torneo.addValueChangeListener(ev -> {
            torneoActual = ev.getValue();
            if (torneoActual != null) {
                recargarFixture(torneoActual);
                btnElim.setVisible(
                        fixtureGeneratorService.puedeGenerarEliminatoria(torneoActual.id()));
            } else {
                contenedorFases.removeAll();
                btnElim.setVisible(false);
            }
        });

        HorizontalLayout campos = new HorizontalLayout(fechaInicio, hora, cancha);
        campos.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout botones = new HorizontalLayout(btnGenerar, btnElim);
        botones.setSpacing(true);

        contenedorFases.setPadding(false);
        contenedorFases.setSpacing(true);

        add(titulo, tituloGenerador, torneo, campos, botones, contenedorFases);
        setPadding(true);
        setSpacing(true);
    }

    private void recargarFixture(TorneoResponse torneo) {
        List<PartidoResponse> partidos = partidoService.getPartidos(torneo.id());
        renderizarPorFases(partidos);
        // Actualizar visibilidad del botón eliminatoria
    }

    private void renderizarPorFases(List<PartidoResponse> partidos) {
        contenedorFases.removeAll();
        if (partidos.isEmpty()) return;

        // Orden de fases para mostrar
        List<String> ordenFases = List.of(
                "LIGA",
                "GRUPOS - Grupo A", "GRUPOS - Grupo B", "GRUPOS - Grupo C",
                "GRUPOS - Grupo D", "GRUPOS - Grupo E", "GRUPOS - Grupo F",
                "DIECISEISAVOS", "DIECISEISAVOS (ida)", "DIECISEISAVOS (vuelta)",
                "OCTAVOS", "OCTAVOS (ida)", "OCTAVOS (vuelta)",
                "CUARTOS DE FINAL", "CUARTOS DE FINAL (ida)", "CUARTOS DE FINAL (vuelta)",
                "SEMIFINAL", "SEMIFINAL (ida)", "SEMIFINAL (vuelta)",
                "FINAL", "FINAL (ida)", "FINAL (vuelta)"
        );

        // Agrupar manteniendo orden
        Map<String, List<PartidoResponse>> porFase = partidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.fase() != null ? p.fase() : "PARTIDOS",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Ordenar según lista de orden definida
        List<String> fasesOrdenadas = porFase.keySet().stream()
                .sorted(Comparator.comparingInt(f -> {
                    int idx = ordenFases.indexOf(f);
                    return idx == -1 ? 999 : idx;
                }))
                .toList();

        for (String fase : fasesOrdenadas) {
            List<PartidoResponse> pFase = porFase.get(fase);
            if (pFase == null || pFase.isEmpty()) continue;

            H3 tituloFase = new H3(fase);
            tituloFase.getStyle()
                    .set("color", "#1a237e")
                    .set("border-bottom", "2px solid var(--lumo-primary-color)")
                    .set("padding-bottom", "4px")
                    .set("margin-top", "16px");

            Grid<PartidoResponse> grid = new Grid<>(PartidoResponse.class, false);
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            grid.addColumn(PartidoResponse::fecha).setHeader("Fecha").setWidth("110px").setFlexGrow(0);
            grid.addColumn(PartidoResponse::hora).setHeader("Hora").setWidth("80px").setFlexGrow(0);
            grid.addColumn(PartidoResponse::equipoLocal).setHeader("Local");
            grid.addColumn(PartidoResponse::equipoVisitante).setHeader("Visitante");
            grid.addColumn(PartidoResponse::cancha).setHeader("Cancha");
            grid.addComponentColumn(p -> {
                Span badge = new Span(p.estado());
                badge.getElement().getThemeList()
                        .add(p.jugado() ? "badge success" : "badge");
                return badge;
            }).setHeader("Estado").setWidth("100px").setFlexGrow(0);

            grid.setItems(pFase);
            grid.setAllRowsVisible(true);
            grid.setWidthFull();

            contenedorFases.add(tituloFase, grid);
        }
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
