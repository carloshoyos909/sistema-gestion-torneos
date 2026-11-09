package com.torneos.view;

import com.torneos.dto.TablaPosicionesDTO;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.ReporteService;
import com.torneos.service.TablaPosicionesService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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

    private final Anchor anchorPdf   = new Anchor();
    private final Anchor anchorExcel = new Anchor();

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

        configurarAnchorDescarga(anchorPdf,   "📄 PDF",   "btn-descarga btn-descarga-pdf disabled");
        configurarAnchorDescarga(anchorExcel, "📊 Excel", "btn-descarga btn-descarga-excel disabled");

        configurarGrid();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            if (torneoSeleccionado == null) {
                grid.setItems();
                datosActuales = new ArrayList<>();
                deshabilitarAnchors();
            } else {
                datosActuales = tablaPosicionesService.obtenerTablaOrdenada(torneoSeleccionado.id());
                grid.setItems(datosActuales);

                if (!datosActuales.isEmpty()) {
                    String nombre   = torneoSeleccionado.nombre();
                    String safeName = nombre.replaceAll("[^a-zA-Z0-9]", "_");

                    anchorPdf.setHref(registrarStream(new StreamResource(
                            safeName + "_posiciones.pdf",
                            () -> reporteService.generarPdfTablaPosiciones(nombre, datosActuales))));
                    anchorExcel.setHref(registrarStream(new StreamResource(
                            safeName + "_posiciones.xlsx",
                            () -> reporteService.generarExcelTablaPosiciones(nombre, datosActuales))));

                    habilitarAnchors();
                } else {
                    deshabilitarAnchors();
                }
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, anchorPdf, anchorExcel);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);

        add(titulo, toolbar, grid);
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
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addComponentColumn(t -> {
            HorizontalLayout celda = new HorizontalLayout();
            celda.setAlignItems(FlexComponent.Alignment.CENTER);
            celda.setSpacing(true);
            if (t.getPosicion() == 1) {
                Span trofeo = new Span("🏆");
                Span badge  = new Span("1");
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
            pts.getStyle()
                    .set("font-weight", "700")
                    .set("color", t.getPosicion() == 1 ? "#00a651" : "#1a237e");
            return pts;
        }).setHeader("PTS").setWidth("70px").setFlexGrow(0);

        grid.setClassNameGenerator(t -> t.getPosicion() == 1 ? "fila-lider" : "");
        grid.setWidthFull();
    }
}
