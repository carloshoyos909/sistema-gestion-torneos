package com.torneos.view;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.PartidoService;
import com.torneos.service.ReporteService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.IntegerField;
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

        // ── Botones de descarga usando StreamResource + Page.open() ───────────
        // Este patrón evita el bug de Vaadin donde Anchor+Button descarga .htm
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
            if (torneoSeleccionado == null || partidosActuales.isEmpty()) return;
            String nombre = torneoSeleccionado.nombre();
            StreamResource resource = new StreamResource(
                    "fixture_" + nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf",
                    () -> reporteService.generarPdfFixture(nombre, partidosActuales));
            descargarRecurso(resource);
        });

        btnExcel.addClickListener(e -> {
            if (torneoSeleccionado == null || partidosActuales.isEmpty()) return;
            String nombre = torneoSeleccionado.nombre();
            StreamResource resource = new StreamResource(
                    "fixture_" + nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx",
                    () -> reporteService.generarExcelFixture(nombre, partidosActuales));
            descargarRecurso(resource);
        });

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
                partidosActuales = partidoService.getPartidos(torneoSeleccionado.id());
                renderizarTarjetas();
                boolean hayPartidos = !partidosActuales.isEmpty();
                btnPdf.setEnabled(hayPartidos);
                btnExcel.setEnabled(hayPartidos);
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(torneo, btnPdf, btnExcel);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setSpacing(true);

        add(toolbar, contenedorTarjetas);
        setPadding(true);
    }

    /**
     * Registra el StreamResource en la sesión de Vaadin y abre la URL
     * directamente — esto garantiza la descarga del archivo real (.pdf / .xlsx)
     * sin que Vaadin lo intercepte como navegación y genere un .htm.
     */
    private void descargarRecurso(StreamResource resource) {
        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());
    }

    private void renderizarTarjetas() {
        contenedorTarjetas.removeAll();

        if (partidosActuales.isEmpty()) {
            Span vacio = new Span("No hay partidos programados para este torneo.");
            vacio.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic")
                    .set("padding", "24px 0");
            contenedorTarjetas.add(vacio);
            return;
        }

        // Agrupar por fase preservando el orden de inserción
        Map<String, List<PartidoResponse>> porFase = new java.util.LinkedHashMap<>();
        for (PartidoResponse p : partidosActuales) {
            String fase = (p.fase() != null && !p.fase().isBlank()) ? p.fase() : "PARTIDOS";
            porFase.computeIfAbsent(fase, k -> new java.util.ArrayList<>()).add(p);
        }

        boolean multiFase = porFase.size() > 1;

        for (Map.Entry<String, List<PartidoResponse>> entry : porFase.entrySet()) {
            // Siempre mostrar el encabezado de fase para dar contexto
            com.vaadin.flow.component.html.H3 tituloFase =
                    new com.vaadin.flow.component.html.H3(entry.getKey());
            tituloFase.getStyle()
                    .set("color", "#1a237e")
                    .set("border-bottom", "2px solid var(--lumo-primary-color)")
                    .set("padding-bottom", "4px")
                    .set("width", "100%")
                    .set("margin-top", multiFase ? "16px" : "0")
                    .set("margin-bottom", "8px");
            contenedorTarjetas.add(tituloFase);

            for (PartidoResponse p : entry.getValue()) {
                contenedorTarjetas.add(crearMatchCard(p));
            }
        }
    }

    private VerticalLayout crearMatchCard(PartidoResponse partido) {
        // Badge de estado
        Span estadoBadge = new Span(partido.estado());
        if (partido.jugado()) {
            estadoBadge.getElement().getThemeList().add("badge success");
        } else {
            boolean pasado = partido.fecha().isBefore(java.time.LocalDate.now());
            estadoBadge.getElement().getThemeList().add(pasado ? "badge error" : "badge");
        }

        // Marcador
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

        // Info inferior
        Paragraph info = new Paragraph("📅 " + partido.fecha() + "  ⏰ " + partido.hora() + "  📍 " + partido.cancha());
        info.addClassName("match-info");

        HorizontalLayout footer = new HorizontalLayout(estadoBadge);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        if (esAdminOOrganizador()) {
            if (torneoSeleccionado != null &&
                    partidoService.puedeModificar(torneoSeleccionado.id())) {
                Button btnResultado = new Button(partido.jugado() ? "✏️" : "⚽",
                        e -> abrirDialogoResultado(partido));
                btnResultado.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                footer.add(btnResultado);
            }
        }

        VerticalLayout card = new VerticalLayout(enfrentamiento, info, footer);
        card.addClassName("match-card");
        card.setPadding(true);
        card.setSpacing(false);
        return card;
    }

    private void abrirDialogoResultado(PartidoResponse partido) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚽ Resultado: " + partido.equipoLocal() + " vs " + partido.equipoVisitante());

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

    private boolean esAdminOOrganizador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_ORGANIZADOR"));
    }
}
