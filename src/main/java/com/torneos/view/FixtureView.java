package com.torneos.view;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.PartidoService;
import com.torneos.service.ReporteService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "fixture", layout = MainView.class)
@PageTitle("Fixture")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class FixtureView extends VerticalLayout {

    private final PartidoService partidoService;
    private final ReporteService reporteService;
    private final Grid<PartidoResponse> grid = new Grid<>(PartidoResponse.class, false);
    private TorneoResponse torneoSeleccionado;
    private List<PartidoResponse> partidosActuales = new ArrayList<>();

    public FixtureView(PartidoService partidoService, TorneoService torneoService,
                       ReporteService reporteService) {
        this.partidoService = partidoService;
        this.reporteService = reporteService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("350px");

        // Botones de exportación
        Button btnPdf = new Button("PDF", new Icon(VaadinIcon.FILE_TEXT));
        btnPdf.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        btnPdf.setEnabled(false);

        Button btnExcel = new Button("Excel", new Icon(VaadinIcon.TABLE));
        btnExcel.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btnExcel.setEnabled(false);

        // Los Anchor se recrean dinámicamente cuando hay torneo seleccionado
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
                partidosActuales = new ArrayList<>();
                btnPdf.setEnabled(false);
                btnExcel.setEnabled(false);
            } else {
                partidosActuales = partidoService.getPartidos(torneoSeleccionado.id());
                grid.setItems(partidosActuales);
                btnPdf.setEnabled(true);
                btnExcel.setEnabled(true);

                String nombre = torneoSeleccionado.nombre();

                StreamResource pdfResource = new StreamResource(
                        "fixture_" + nombre.replaceAll("\\s+", "_") + ".pdf",
                        () -> reporteService.generarPdfFixture(nombre, partidosActuales));
                anchorPdf.setHref(pdfResource);

                StreamResource excelResource = new StreamResource(
                        "fixture_" + nombre.replaceAll("\\s+", "_") + ".xlsx",
                        () -> reporteService.generarExcelFixture(nombre, partidosActuales));
                anchorExcel.setHref(excelResource);
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, anchorPdf, anchorExcel);
        toolbar.setAlignItems(Alignment.END);

        add(toolbar, grid);
        setPadding(true);
    }

    private void configurarGrid() {
        grid.addColumn(PartidoResponse::fecha).setHeader("Fecha").setSortable(true);
        grid.addColumn(PartidoResponse::hora).setHeader("Hora");
        grid.addColumn(PartidoResponse::equipoLocal).setHeader("Local");
        grid.addColumn(PartidoResponse::equipoVisitante).setHeader("Visitante");
        grid.addColumn(PartidoResponse::cancha).setHeader("Cancha");

        grid.addComponentColumn(partido -> {
            Span badge = new Span(partido.estado());
            if (partido.jugado()) {
                badge.getElement().getThemeList().add("badge success");
            } else {
                boolean esPasado = partido.fecha().isBefore(java.time.LocalDate.now());
                badge.getElement().getThemeList().add(esPasado ? "badge error" : "badge");
            }
            return badge;
        }).setHeader("Estado");

        grid.addComponentColumn(partido -> {
            if (!esAdminOOrganizador()) return new Span();
            Button btn = new Button(partido.jugado() ? "✏️ Editar" : "⚽ Resultado");
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.addClickListener(e -> abrirDialogoResultado(partido));
            return btn;
        }).setHeader("Acciones");

        grid.setWidthFull();
    }

    private void abrirDialogoResultado(PartidoResponse partido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registrar resultado");

        Span info = new Span(partido.equipoLocal() + " vs " + partido.equipoVisitante());
        info.getStyle().set("font-weight", "bold");

        IntegerField golesLocal = new IntegerField(partido.equipoLocal());
        golesLocal.setMin(0);
        golesLocal.setValue(partido.golesLocal() != null ? partido.golesLocal() : 0);
        golesLocal.setStepButtonsVisible(true);

        IntegerField golesVisitante = new IntegerField(partido.equipoVisitante());
        golesVisitante.setMin(0);
        golesVisitante.setValue(partido.golesVisitante() != null ? partido.golesVisitante() : 0);
        golesVisitante.setStepButtonsVisible(true);

        Button btnGuardar = new Button("Guardar", e -> {
            try {
                partidoService.registrarResultado(partido.id(),
                        golesLocal.getValue(), golesVisitante.getValue());
                Notification n = Notification.show("Resultado guardado.");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (torneoSeleccionado != null) {
                    partidosActuales = partidoService.getPartidos(torneoSeleccionado.id());
                    grid.setItems(partidosActuales);
                }
                dialog.close();
            } catch (IllegalArgumentException ex) {
                Notification n = Notification.show(ex.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        dialog.add(new VerticalLayout(info,
                new HorizontalLayout(golesLocal, golesVisitante),
                new HorizontalLayout(btnGuardar, btnCancelar)));
        dialog.open();
    }

    private boolean esAdminOOrganizador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_ORGANIZADOR"));
    }
}
