package com.torneos.view;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.PartidoService;
import com.torneos.service.ReporteService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Route(value = "fixture", layout = MainView.class)
@PageTitle("Fixture")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class FixtureView extends VerticalLayout {

    private final PartidoService partidoService;
    private final ReporteService reporteService;
    private TorneoResponse torneoSeleccionado;
    private List<PartidoResponse> partidosActuales = new ArrayList<>();
    private final FlexLayout contenedorTarjetas = new FlexLayout();

    public FixtureView(PartidoService partidoService, TorneoService torneoService,
                       ReporteService reporteService) {
        this.partidoService = partidoService;
        this.reporteService = reporteService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidth("350px");

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

        contenedorTarjetas.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contenedorTarjetas.getStyle().set("gap", "16px").set("padding", "8px 0");
        contenedorTarjetas.setWidthFull();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            if (torneoSeleccionado == null) {
                contenedorTarjetas.removeAll();
                partidosActuales = new ArrayList<>();
                btnPdf.setEnabled(false);
                btnExcel.setEnabled(false);
            } else {
                // LECTURA LIBRE — sin validación de propietario
                partidosActuales = partidoService.getPartidos(torneoSeleccionado.id());
                renderizarTarjetas();
                btnPdf.setEnabled(true);
                btnExcel.setEnabled(true);
                String nombre = torneoSeleccionado.nombre();
                anchorPdf.setHref(new StreamResource(
                        "fixture_" + nombre.replaceAll("\\s+", "_") + ".pdf",
                        () -> reporteService.generarPdfFixture(nombre, partidosActuales)));
                anchorExcel.setHref(new StreamResource(
                        "fixture_" + nombre.replaceAll("\\s+", "_") + ".xlsx",
                        () -> reporteService.generarExcelFixture(nombre, partidosActuales)));
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, anchorPdf, anchorExcel);
        toolbar.setAlignItems(FlexComponent.Alignment.END);

        add(toolbar, contenedorTarjetas);
        setPadding(true);
    }

    private void renderizarTarjetas() {
        contenedorTarjetas.removeAll();
        if (partidosActuales == null || partidosActuales.isEmpty()) return;

        // Calcular puedeModificar LOCALMENTE sin consultar la BD
        // Solo usa el SecurityContext (ya cargado) y el creadorUsername del DTO
        boolean puedeEditar = calcularPuedeEditar();

        // Agrupar por fase
        Map<String, List<PartidoResponse>> porFase = new LinkedHashMap<>();
        for (PartidoResponse p : partidosActuales) {
            String fase = p.fase() != null ? p.fase() : "PARTIDOS";
            porFase.computeIfAbsent(fase, k -> new ArrayList<>()).add(p);
        }

        boolean multiFase = porFase.size() > 1;

        for (Map.Entry<String, List<PartidoResponse>> entry : porFase.entrySet()) {
            if (multiFase) {
                H3 tituloFase = new H3(entry.getKey());
                tituloFase.getStyle()
                        .set("color", "#1a237e")
                        .set("border-bottom", "2px solid var(--lumo-primary-color)")
                        .set("padding-bottom", "4px")
                        .set("width", "100%")
                        .set("margin-top", "16px");
                contenedorTarjetas.add(tituloFase);
            }
            for (PartidoResponse p : entry.getValue()) {
                try {
                    contenedorTarjetas.add(crearMatchCard(p, puedeEditar));
                } catch (Exception ex) {
                    // Aislamiento: si falla una tarjeta no destruye el resto
                    System.err.println("Error renderizando partido " + p.id() + ": " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Calcula si el usuario actual puede editar partidos de este torneo.
     * NO hace consultas a la BD — usa solo el SecurityContext y el DTO.
     * ADMIN → siempre puede.
     * ORGANIZADOR → solo si es el creador del torneo.
     * JUGADOR → nunca puede.
     */
    private boolean calcularPuedeEditar() {
        if (torneoSeleccionado == null) return false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        String username = auth.getName();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) return true;

        boolean esOrganizador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZADOR"));
        if (!esOrganizador) return false;

        // Organizador solo puede si es el creador del torneo
        String creador = torneoSeleccionado.creadorUsername();
        return creador != null && creador.equals(username);
    }

    private VerticalLayout crearMatchCard(PartidoResponse partido, boolean puedeEditar) {
        Span estadoBadge = new Span(partido.estado());
        if (partido.jugado()) {
            estadoBadge.getElement().getThemeList().add("badge success");
        } else {
            boolean pasado = partido.fecha().isBefore(java.time.LocalDate.now());
            estadoBadge.getElement().getThemeList().add(pasado ? "badge error" : "badge");
        }

        Span localNombre = new Span(partido.equipoLocal());
        localNombre.addClassName("match-team");

        Span marcador = new Span(partido.jugado() && partido.golesLocal() != null
                ? partido.golesLocal() + " - " + partido.golesVisitante()
                : "vs");
        marcador.addClassName("match-score");

        Span visitanteNombre = new Span(partido.equipoVisitante());
        visitanteNombre.addClassName("match-team");

        HorizontalLayout enfrentamiento = new HorizontalLayout(localNombre, marcador, visitanteNombre);
        enfrentamiento.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        enfrentamiento.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        enfrentamiento.setWidthFull();
        enfrentamiento.setSpacing(true);

        Paragraph info = new Paragraph("📅 " + partido.fecha() + "  ⏰ " + partido.hora() + "  📍 " + partido.cancha());
        info.addClassName("match-info");

        HorizontalLayout footer = new HorizontalLayout(estadoBadge);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        if (puedeEditar) {
            Button btnResultado = new Button(partido.jugado() ? "✏️" : "⚽",
                    e -> abrirDialogoResultado(partido));
            btnResultado.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            footer.add(btnResultado);
        }

        VerticalLayout card = new VerticalLayout(enfrentamiento, info, footer);
        card.addClassName("match-card");
        card.setPadding(true);
        card.setSpacing(false);
        return card;
    }

    private void abrirDialogoResultado(PartidoResponse partido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚽ " + partido.equipoLocal() + " vs " + partido.equipoVisitante());

        IntegerField golesLocal = new IntegerField(partido.equipoLocal());
        golesLocal.setMin(0);
        golesLocal.setValue(partido.golesLocal() != null ? partido.golesLocal() : 0);
        golesLocal.setStepButtonsVisible(true);

        IntegerField golesVisitante = new IntegerField(partido.equipoVisitante());
        golesVisitante.setMin(0);
        golesVisitante.setValue(partido.golesVisitante() != null ? partido.golesVisitante() : 0);
        golesVisitante.setStepButtonsVisible(true);

        Button btnGuardar = new Button("Guardar", e -> {
            int gl = golesLocal.getValue() != null ? golesLocal.getValue() : 0;
            int gv = golesVisitante.getValue() != null ? golesVisitante.getValue() : 0;
            try {
                partidoService.registrarResultado(partido.id(), gl, gv);
                Notification n = Notification.show("Resultado guardado.");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (torneoSeleccionado != null) {
                    partidosActuales = partidoService.getPartidos(torneoSeleccionado.id());
                    renderizarTarjetas();
                }
                dialog.close();
            } catch (IllegalArgumentException ex) {
                Notification n = Notification.show(ex.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (SecurityException ex) {
                Notification n = Notification.show("No tienes permiso para modificar este partido.",
                        3000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                dialog.close();
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        dialog.add(new VerticalLayout(
                new HorizontalLayout(golesLocal, golesVisitante),
                new HorizontalLayout(btnGuardar, btnCancelar)));
        dialog.open();
    }
}
