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
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
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

    private final Anchor anchorPdf   = new Anchor();
    private final Anchor anchorExcel = new Anchor();

    public JugadorView(EstadisticaService estadisticaService,
                       TorneoService torneoService,
                       ReporteService reporteService) {
        this.estadisticaService = estadisticaService;
        this.reporteService = reporteService;

        H2 titulo = new H2("⚽ Panel de Jugador — Estadísticas");

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("280px");

        TextField buscarJugador = new TextField();
        buscarJugador.setPlaceholder("🔍 Buscar jugador...");
        buscarJugador.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscarJugador.setWidth("200px");
        buscarJugador.setClearButtonVisible(true);

        configurarAnchorDescarga(anchorPdf,   "📄 PDF",   "btn-descarga btn-descarga-pdf disabled");
        configurarAnchorDescarga(anchorExcel, "📊 Excel", "btn-descarga btn-descarga-excel disabled");

        HorizontalLayout filtros = new HorizontalLayout(torneo, buscarJugador, anchorPdf, anchorExcel);
        filtros.setAlignItems(FlexComponent.Alignment.CENTER);
        filtros.getStyle()
                .set("background", "rgba(0,0,0,0.03)")
                .set("padding", "12px 16px")
                .set("border-radius", "10px")
                .set("margin-bottom", "8px");
        filtros.setWidthFull();

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

    private void configurarAnchorDescarga(Anchor anchor, String texto, String clases) {
        anchor.getElement().setAttribute("router-ignore", "");
        anchor.getElement().setAttribute("download", "");
        anchor.setText(texto);
        anchor.setClassName(clases);
        anchor.setHref("");
    }

    private void habilitarAnchors() {
        anchorPdf.setClassName("btn-descarga btn-descarga-pdf");
        anchorExcel.setClassName("btn-descarga btn-descarga-excel");
    }

    private void deshabilitarAnchors() {
        anchorPdf.setClassName("btn-descarga btn-descarga-pdf disabled");
        anchorExcel.setClassName("btn-descarga btn-descarga-excel disabled");
        anchorPdf.setHref("");
        anchorExcel.setHref("");
    }

    private String registrarStream(StreamResource resource) {
        StreamRegistration reg = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(resource);
        return reg.getResourceUri().toString();
    }

    private void configurarGrid() {
        grid.addColumn(EstadisticaResponse::jugador).setHeader("Jugador").setSortable(true);
        grid.addColumn(EstadisticaResponse::equipo).setHeader("Equipo").setSortable(true);
        grid.addColumn(EstadisticaResponse::goles).setHeader("Goles").setSortable(true);
        grid.addColumn(EstadisticaResponse::asistencias).setHeader("Asistencias").setSortable(true);
        grid.addColumn(EstadisticaResponse::partidosJugados).setHeader("Partidos Jugados");
        grid.addColumn(EstadisticaResponse::tarjetasAmarillas).setHeader("🟡 Amarillas").setSortable(true);
        grid.addColumn(EstadisticaResponse::tarjetasRojas).setHeader("🔴 Rojas").setSortable(true);

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
        dialog.setHeaderTitle("✏️ Estadísticas — " + est.jugador());
        dialog.setWidth("420px");

        H3 subtitulo = new H3("Equipo: " + est.equipo());
        subtitulo.getStyle()
                .set("margin", "0 0 12px 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        ComboBox<Jugador> selectorJugador = new ComboBox<>("Seleccionar jugador");
        selectorJugador.setItemLabelGenerator(j -> j.getNombre() + " — " + j.getEquipo().getNombre());
        selectorJugador.setWidthFull();
        try {
            selectorJugador.setItems(estadisticaService.getJugadores(torneoSeleccionado.id()));
        } catch (Exception ex) {
            selectorJugador.setItems(List.of());
        }
        try {
            estadisticaService.getJugadores(torneoSeleccionado.id()).stream()
                    .filter(j -> j.getId().equals(est.jugadorId()))
                    .findFirst()
                    .ifPresent(selectorJugador::setValue);
        } catch (Exception ignored) {}

        IntegerField goles       = campo("Goles",          est.goles(),            "120px");
        IntegerField asistencias = campo("Asistencias",    est.asistencias(),       "120px");
        IntegerField partidos    = campo("Partidos",       est.partidosJugados(),   "120px");
        IntegerField amarillas   = campo("🟡 Amarillas",   est.tarjetasAmarillas(), "120px");
        IntegerField rojas       = campo("🔴 Rojas",       est.tarjetasRojas(),     "120px");

        Button btnGuardar = new Button("Guardar", e -> {
            Jugador jugadorSel = selectorJugador.getValue();
            if (jugadorSel == null) {
                Notification.show("Selecciona un jugador.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                estadisticaService.actualizarEstadistica(
                        jugadorSel.getId(), torneoSeleccionado.id(),
                        goles.getValue(), asistencias.getValue(),
                        partidos.getValue(), amarillas.getValue(), rojas.getValue());
                Notification.show("Guardado correctamente.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                actualizarGridSilencioso();
                dialog.close();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                subtitulo, selectorJugador,
                new HorizontalLayout(goles, asistencias, partidos),
                new HorizontalLayout(amarillas, rojas),
                new HorizontalLayout(btnGuardar, btnCancelar)));
        dialog.open();
    }

    private IntegerField campo(String label, int valor, String width) {
        IntegerField f = new IntegerField(label);
        f.setMin(0);
        f.setValue(valor);
        f.setStepButtonsVisible(true);
        f.setWidth(width);
        return f;
    }

    private void actualizarGrid(String filtroNombre) {
        if (torneoSeleccionado == null) {
            grid.setItems();
            datosActuales = new ArrayList<>();
            deshabilitarAnchors();
            return;
        }

        datosActuales = estadisticaService.getEstadisticas(torneoSeleccionado.id(), filtroNombre);
        grid.setItems(datosActuales);

        if (!datosActuales.isEmpty()) {
            String nombre   = torneoSeleccionado.nombre();
            String safeName = nombre.replaceAll("[^a-zA-Z0-9]", "_");

            anchorPdf.setHref(registrarStream(new StreamResource(
                    safeName + "_estadisticas.pdf",
                    () -> reporteService.generarPdfEstadisticas(nombre, datosActuales))));
            anchorExcel.setHref(registrarStream(new StreamResource(
                    safeName + "_estadisticas.xlsx",
                    () -> reporteService.generarExcelEstadisticas(nombre, datosActuales))));

            habilitarAnchors();
        } else {
            deshabilitarAnchors();
        }
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
