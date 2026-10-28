package com.torneos.view;

import com.torneos.dto.EstadisticaResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.model.Jugador;
import com.torneos.service.EstadisticaService;
import com.torneos.service.ReporteService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Route(value = "jugador", layout = MainView.class)
@PageTitle("Panel Jugador")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class JugadorView extends VerticalLayout {

    private final EstadisticaService estadisticaService;
    private final ReporteService reporteService;
    private final Grid<EstadisticaResponse> grid = new Grid<>(EstadisticaResponse.class, false);
    private TorneoResponse torneoSeleccionado;
    private List<EstadisticaResponse> datosActuales = new ArrayList<>();

    public JugadorView(EstadisticaService estadisticaService,
                       TorneoService torneoService,
                       ReporteService reporteService) {
        this.estadisticaService = estadisticaService;
        this.reporteService = reporteService;

        H2 titulo = new H2("⚽ Panel de Jugador — Estadísticas");

        // ── Filtros ──────────────────────────────────────────────────────────
        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("280px");

        TextField buscarJugador = new TextField();
        buscarJugador.setPlaceholder("Buscar jugador...");
        buscarJugador.setWidth("200px");
        buscarJugador.setClearButtonVisible(true);

        // ── Botones exportar ─────────────────────────────────────────────────
        Button btnPdf = new Button("PDF", new Icon(VaadinIcon.FILE_TEXT));
        btnPdf.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        btnPdf.setEnabled(false);

        Button btnExcel = new Button("Excel", new Icon(VaadinIcon.TABLE));
        btnExcel.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnExcel.setEnabled(false);

        Anchor anchorPdf = new Anchor("", "");
        anchorPdf.getElement().setAttribute("download", true);
        anchorPdf.add(btnPdf);
        anchorPdf.getStyle().set("text-decoration", "none");

        Anchor anchorExcel = new Anchor("", "");
        anchorExcel.getElement().setAttribute("download", true);
        anchorExcel.add(btnExcel);
        anchorExcel.getStyle().set("text-decoration", "none");

        HorizontalLayout filtros = new HorizontalLayout(torneo, buscarJugador,
                anchorPdf, anchorExcel);
        filtros.setAlignItems(FlexComponent.Alignment.END);

        // ── Grid ─────────────────────────────────────────────────────────────
        configurarGrid();

        // ── Eventos ──────────────────────────────────────────────────────────
        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            actualizarGrid(buscarJugador.getValue(), anchorPdf, anchorExcel, btnPdf, btnExcel);
        });

        buscarJugador.addValueChangeListener(event ->
                actualizarGrid(event.getValue(), anchorPdf, anchorExcel, btnPdf, btnExcel));

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

        // Columna de edición — solo admin y organizador
        grid.addComponentColumn(est -> {
            if (!esAdminOOrganizador()) return new com.vaadin.flow.component.html.Span();
            Button btn = new Button("✏️ Editar");
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> abrirDialogoEditar(est));
            return btn;
        }).setHeader("Acciones");

        grid.setWidthFull();
    }

    private void abrirDialogoEditar(EstadisticaResponse est) {
        if (torneoSeleccionado == null) return;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✏️ Editar estadísticas — " + est.jugador());
        dialog.setWidth("420px");

        H3 subtitulo = new H3("Equipo: " + est.equipo());
        subtitulo.getStyle().set("margin", "0 0 12px 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        // Búsqueda de jugador dentro del diálogo
        ComboBox<Jugador> selectorJugador = new ComboBox<>("Buscar y seleccionar jugador");
        selectorJugador.setItemLabelGenerator(j -> j.getNombre() + " — " + j.getEquipo().getNombre());
        selectorJugador.setWidthFull();
        try {
            selectorJugador.setItems(estadisticaService.getJugadores(torneoSeleccionado.id()));
        } catch (Exception ex) {
            selectorJugador.setItems(List.of());
        }

        // Seleccionar automáticamente el jugador del registro actual
        try {
            estadisticaService.getJugadores(torneoSeleccionado.id()).stream()
                    .filter(j -> j.getId().equals(est.jugadorId()))
                    .findFirst()
                    .ifPresent(selectorJugador::setValue);
        } catch (Exception ignored) {}

        IntegerField goles = new IntegerField("Goles");
        goles.setMin(0);
        goles.setValue(est.goles());
        goles.setStepButtonsVisible(true);
        goles.setWidth("120px");

        IntegerField asistencias = new IntegerField("Asistencias");
        asistencias.setMin(0);
        asistencias.setValue(est.asistencias());
        asistencias.setStepButtonsVisible(true);
        asistencias.setWidth("120px");

        IntegerField partidosJugados = new IntegerField("Partidos Jugados");
        partidosJugados.setMin(0);
        partidosJugados.setValue(est.partidosJugados());
        partidosJugados.setStepButtonsVisible(true);
        partidosJugados.setWidth("140px");

        IntegerField amarillas = new IntegerField("🟡 Amarillas");
        amarillas.setMin(0);
        amarillas.setValue(est.tarjetasAmarillas());
        amarillas.setStepButtonsVisible(true);
        amarillas.setWidth("120px");

        IntegerField rojas = new IntegerField("🔴 Rojas");
        rojas.setMin(0);
        rojas.setValue(est.tarjetasRojas());
        rojas.setStepButtonsVisible(true);
        rojas.setWidth("120px");

        HorizontalLayout fila1 = new HorizontalLayout(goles, asistencias, partidosJugados);
        HorizontalLayout fila2 = new HorizontalLayout(amarillas, rojas);

        Button btnGuardar = new Button("Guardar", e -> {
            Jugador jugadorSel = selectorJugador.getValue();
            if (jugadorSel == null) {
                Notification n = Notification.show("Selecciona un jugador.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                estadisticaService.actualizarEstadistica(
                        jugadorSel.getId(),
                        torneoSeleccionado.id(),
                        goles.getValue(),
                        asistencias.getValue(),
                        partidosJugados.getValue(),
                        amarillas.getValue(),
                        rojas.getValue()
                );
                Notification n = Notification.show("Estadísticas guardadas correctamente.");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                actualizarGridSilencioso();
                dialog.close();
            } catch (IllegalArgumentException ex) {
                Notification n = Notification.show(ex.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        VerticalLayout contenido = new VerticalLayout(subtitulo, selectorJugador,
                fila1, fila2, new HorizontalLayout(btnGuardar, btnCancelar));
        contenido.setPadding(false);
        contenido.setSpacing(true);

        dialog.add(contenido);
        dialog.open();
    }

    private void actualizarGrid(String filtroNombre, Anchor anchorPdf, Anchor anchorExcel,
                                 Button btnPdf, Button btnExcel) {
        if (torneoSeleccionado == null) {
            grid.setItems();
            datosActuales = new ArrayList<>();
            btnPdf.setEnabled(false);
            btnExcel.setEnabled(false);
            return;
        }

        datosActuales = estadisticaService.getEstadisticas(torneoSeleccionado.id(), filtroNombre);
        grid.setItems(datosActuales);
        btnPdf.setEnabled(true);
        btnExcel.setEnabled(true);

        String nombre = torneoSeleccionado.nombre();
        anchorPdf.setHref(new StreamResource(
                "estadisticas_" + nombre.replaceAll("\\s+", "_") + ".pdf",
                () -> reporteService.generarPdfEstadisticas(nombre, datosActuales)));
        anchorExcel.setHref(new StreamResource(
                "estadisticas_" + nombre.replaceAll("\\s+", "_") + ".xlsx",
                () -> reporteService.generarExcelEstadisticas(nombre, datosActuales)));
    }

    private void actualizarGridSilencioso() {
        if (torneoSeleccionado != null) {
            datosActuales = estadisticaService.getEstadisticas(torneoSeleccionado.id(), "");
            grid.setItems(datosActuales);
        }
    }

    private boolean esAdminOOrganizador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_ORGANIZADOR"));
    }
}
