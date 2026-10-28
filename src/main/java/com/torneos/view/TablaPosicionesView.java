package com.torneos.view;

import com.torneos.dto.TablaPosicionesDTO;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.ReporteService;
import com.torneos.service.TablaPosicionesService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
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

        H2 titulo = new H2("🏆 Tabla de Posiciones");

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("300px");

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
                btnPdf.setEnabled(true);
                btnExcel.setEnabled(true);

                String nombre = torneoSeleccionado.nombre();
                anchorPdf.setHref(new StreamResource(
                        "posiciones_" + nombre.replaceAll("\\s+", "_") + ".pdf",
                        () -> reporteService.generarPdfTablaPosiciones(nombre, datosActuales)));
                anchorExcel.setHref(new StreamResource(
                        "posiciones_" + nombre.replaceAll("\\s+", "_") + ".xlsx",
                        () -> reporteService.generarExcelTablaPosiciones(nombre, datosActuales)));
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, anchorPdf, anchorExcel);
        toolbar.setAlignItems(FlexComponent.Alignment.END);

        add(titulo, toolbar, grid);
        setPadding(true);
        setSpacing(true);
    }

    private void configurarGrid() {
        // Columna Pos con badge visual
        grid.addComponentColumn(t -> {
            Span badge = new Span(String.valueOf(t.getPosicion()));
            if (t.getPosicion() == 1) badge.getElement().getThemeList().add("badge success");
            else if (t.getPosicion() == 2) badge.getElement().getThemeList().add("badge contrast");
            else badge.getElement().getThemeList().add("badge");
            return badge;
        }).setHeader("Pos").setWidth("70px").setFlexGrow(0);

        grid.addColumn(TablaPosicionesDTO::getEquipo).setHeader("Equipo").setSortable(true);
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
            pts.getStyle().set("font-weight", "bold");
            return pts;
        }).setHeader("PTS").setWidth("70px").setFlexGrow(0).setSortable(false);

        grid.setWidthFull();
    }
}
