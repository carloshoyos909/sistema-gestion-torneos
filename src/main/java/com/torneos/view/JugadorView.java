package com.torneos.view;

import com.torneos.dto.EstadisticaResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.model.Jugador;
import com.torneos.service.EstadisticaService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

        HorizontalLayout filtros = new HorizontalLayout(torneo);
        filtros.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        configurarGrid();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            actualizarGrid();
        });

        add(titulo, filtros);
        if (puedeEditarEstadisticas()) {
            add(crearEditor(torneo));
        }
        add(grid);
        setPadding(true);
        setSpacing(true);
    }

    private boolean puedeEditarEstadisticas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_ORGANIZADOR"));
    }

    private VerticalLayout crearEditor(ComboBox<TorneoResponse> torneo) {
        VerticalLayout editor = new VerticalLayout();
        editor.setPadding(false);
        editor.setWidthFull();

        ComboBox<Jugador> jugador = new ComboBox<>("Jugador");
        jugador.setItemLabelGenerator(j -> j.getNombre() + " — " + j.getEquipo().getNombre());
        jugador.setWidth("350px");

        IntegerField goles = campoEntero("Goles", 0);
        IntegerField asistencias = campoEntero("Asistencias", 0);
        IntegerField partidos = campoEntero("Partidos jugados", 0);
        IntegerField amarillas = campoEntero("Tarjetas amarillas", 0);
        IntegerField rojas = campoEntero("Tarjetas rojas", 0);

        Button guardar = new Button("Guardar estadísticas", event -> {
            if (torneo.getValue() == null || jugador.getValue() == null) {
                mostrar("Selecciona el torneo y el jugador.", NotificationVariant.LUMO_ERROR);
                return;
            }
            if (goles.isEmpty() || asistencias.isEmpty() || partidos.isEmpty()
                    || amarillas.isEmpty() || rojas.isEmpty()) {
                mostrar("Todos los valores estadísticos son obligatorios.", NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                estadisticaService.actualizarEstadistica(
                        jugador.getValue().getId(), torneo.getValue().id(),
                        goles.getValue(), asistencias.getValue(), partidos.getValue(),
                        amarillas.getValue(), rojas.getValue());
                actualizarGrid();
                mostrar("Estadísticas guardadas correctamente.", NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException ex) {
                mostrar(ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        torneo.addValueChangeListener(event -> {
            jugador.clear();
            jugador.setItems(event.getValue() == null
                    ? java.util.List.of()
                    : estadisticaService.getJugadores(event.getValue().id()));
        });
        if (torneo.getValue() != null) {
            jugador.setItems(estadisticaService.getJugadores(torneo.getValue().id()));
        }

        HorizontalLayout campos = new HorizontalLayout(
                jugador, goles, asistencias, partidos, amarillas, rojas, guardar);
        campos.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        editor.add(campos);
        return editor;
    }

    private IntegerField campoEntero(String etiqueta, int valorInicial) {
        IntegerField campo = new IntegerField(etiqueta);
        campo.setMin(0);
        campo.setValue(valorInicial);
        campo.setStepButtonsVisible(true);
        campo.setWidth("140px");
        return campo;
    }

    private void mostrar(String mensaje, NotificationVariant variante) {
        Notification notification = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variante);
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

    private void actualizarGrid() {
        if (torneoSeleccionado == null) {
            grid.setItems();
            return;
        }
        grid.setItems(estadisticaService.getEstadisticas(torneoSeleccionado.id(), null));
    }
}
