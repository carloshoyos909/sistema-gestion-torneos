package com.torneos.view;

import com.torneos.dto.TablaPosicionesDTO;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.ReporteService;
import com.torneos.service.TablaPosicionesService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value = "tabla-posiciones", layout = MainView.class)
@PageTitle("Tabla de Posiciones")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class TablaPosicionesView extends VerticalLayout {

    private final TablaPosicionesService tablaPosicionesService;
    private final ReporteService reporteService;
    private final Grid<TablaPosicionesDTO> grid = new Grid<>(TablaPosicionesDTO.class, false);
    private TorneoResponse torneoSeleccionado;
    private List<TablaPosicionesDTO> datosActuales = new ArrayList<>();

    public TablaPosicionesView(TablaPosicionesService tablaPosicionesService,
                                TorneoService torneoService,
                                ReporteService reporteService) {
        this.tablaPosicionesService = tablaPosicionesService;
        this.reporteService = reporteService;

        H2 titulo = new H2("🥇 Tabla de Posiciones");

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("300px");

        Button btnPdf = new Button("⬇ PDF", new Icon(VaadinIcon.FILE_TEXT));
        btnPdf.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        btnPdf.getStyle()
                .set("background", "#1a237e")
                .set("color", "#ffffff");
        btnPdf.setEnabled(false);

        Button btnExcel = new Button("⬇ Excel", new Icon(VaadinIcon.TABLE));
        btnExcel.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnExcel.setEnabled(false);

        btnPdf.addClickListener(e -> {
            if (torneoSeleccionado == null || datosActuales.isEmpty()) return;
            String nombre = torneoSeleccionado.nombre();
            descargarRecurso(new StreamResource(
                    "posiciones_" + nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf",
                    () -> reporteService.generarPdfTablaPosiciones(nombre, datosActuales)));
        });

        btnExcel.addClickListener(e -> {
            if (torneoSeleccionado == null || datosActuales.isEmpty()) return;
            String nombre = torneoSeleccionado.nombre();
            descargarRecurso(new StreamResource(
                    "posiciones_" + nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx",
                    () -> reporteService.generarExcelTablaPosiciones(nombre, datosActuales)));
        });

        configurarGrid();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            if (torneoSeleccionado == null) {
                grid.setItems();
                datosActuales = new ArrayList<>();
                btnPdf.setEnabled(false);
                btnExcel.setEnabled(false);
            } else {
                datosActuales = tablaPosicionesService.obtenerTablaOrdenada(torneoSeleccionado.id());
                grid.setItems(datosActuales);
                boolean hayDatos = !datosActuales.isEmpty();
                btnPdf.setEnabled(hayDatos);
                btnExcel.setEnabled(hayDatos);
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, btnPdf, btnExcel);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setSpacing(true);

        add(titulo, toolbar, grid);
        setPadding(true);
        setSpacing(true);
    }

    private void descargarRecurso(StreamResource resource) {
        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());
    }

    private void configurarGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Pos con badge especial para el líder
        grid.addComponentColumn(t -> {
            HorizontalLayout celda = new HorizontalLayout();
            celda.setAlignItems(FlexComponent.Alignment.CENTER);
            celda.setSpacing(true);

            if (t.getPosicion() == 1) {
                Span trofeo = new Span("🏆");
                Span badge = new Span("1");
                badge.getElement().getThemeList().add("badge success");
                badge.getStyle().set("font-weight", "700");
                celda.add(trofeo, badge);
            } else if (t.getPosicion() == 2) {
                Span badge = new Span("2");
                badge.getElement().getThemeList().add("badge contrast");
                celda.add(badge);
            } else {
                Span badge = new Span(String.valueOf(t.getPosicion()));
                badge.getElement().getThemeList().add("badge");
                celda.add(badge);
            }
            return celda;
        }).setHeader("Pos").setWidth("90px").setFlexGrow(0);

        // Nombre del equipo — el líder en negrita y color dorado
        grid.addComponentColumn(t -> {
            Span nombre = new Span(t.getEquipo());
            if (t.getPosicion() == 1) {
                nombre.getStyle().set("font-weight", "700").set("color", "#e65100");
            }
            return nombre;
        }).setHeader("Equipo").setSortable(true).setFlexGrow(2);

        grid.addColumn(TablaPosicionesDTO::getPartidosJugados).setHeader("PJ").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TablaPosicionesDTO::getPartidosGanados).setHeader("PG").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TablaPosicionesDTO::getPartidosEmpatados).setHeader("PE").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TablaPosicionesDTO::getPartidosPerdidos).setHeader("PP").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TablaPosicionesDTO::getGolesFavor).setHeader("GF").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TablaPosicionesDTO::getGolesContra).setHeader("GC").setWidth("60px").setFlexGrow(0);

        grid.addColumn(t -> {
            int dg = t.getDiferenciaGoles();
            return dg >= 0 ? "+" + dg : String.valueOf(dg);
        }).setHeader("DG").setWidth("70px").setFlexGrow(0).setSortable(true);

        grid.addComponentColumn(t -> {
            Span pts = new Span(String.valueOf(t.getPuntos()));
            pts.getStyle().set("font-weight", "700")
                    .set("color", t.getPosicion() == 1 ? "#00a651" : "#1a237e");
            return pts;
        }).setHeader("PTS").setWidth("70px").setFlexGrow(0);

        // Clase CSS para resaltar la fila del líder
        grid.setClassNameGenerator(t ->
                t.getPosicion() == 1 ? "fila-lider" : "");

        grid.setWidthFull();
    }
}
