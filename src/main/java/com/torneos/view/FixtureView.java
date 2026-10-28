package com.torneos.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.PartidoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "fixture", layout = MainView.class)
@PageTitle("Fixture")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class FixtureView extends VerticalLayout {

    private final PartidoService partidoService;
    private final Grid<PartidoResponse> grid = new Grid<>(PartidoResponse.class, false);
    private TorneoResponse torneoSeleccionado;

    public FixtureView(PartidoService partidoService, TorneoService torneoService) {
        this.partidoService = partidoService;

        ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.setWidthFull();

        configurarGrid();

        torneo.addValueChangeListener(event -> {
            torneoSeleccionado = event.getValue();
            if (torneoSeleccionado == null) {
                grid.setItems();
            } else {
                grid.setItems(partidoService.getPartidos(torneoSeleccionado.id()));
            }
        });

        add(torneo, grid);
        setPadding(true);
    }

    private void configurarGrid() {
        grid.addColumn(PartidoResponse::fecha).setHeader("Fecha").setSortable(true);
        grid.addColumn(PartidoResponse::hora).setHeader("Hora");
        grid.addColumn(PartidoResponse::equipoLocal).setHeader("Local");
        grid.addColumn(PartidoResponse::equipoVisitante).setHeader("Visitante");
        grid.addColumn(PartidoResponse::cancha).setHeader("Cancha");

        // Columna de estado con badge visual
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

        // Columna de acciones — solo admin y organizador pueden registrar resultado
        grid.addComponentColumn(partido -> {
            if (!esAdminOOrganizador()) return new Span();

            Button btnResultado = new Button(partido.jugado() ? "✏️ Editar" : "⚽ Resultado");
            btnResultado.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnResultado.addClickListener(e -> abrirDialogoResultado(partido));
            return btnResultado;
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
                Notification n = Notification.show("Resultado guardado correctamente.");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                if (torneoSeleccionado != null) {
                    grid.setItems(partidoService.getPartidos(torneoSeleccionado.id()));
                }
                dialog.close();
            } catch (IllegalArgumentException ex) {
                Notification n = Notification.show(ex.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout goles = new HorizontalLayout(golesLocal, golesVisitante);
        HorizontalLayout botones = new HorizontalLayout(btnGuardar, btnCancelar);
        dialog.add(new VerticalLayout(info, goles, botones));
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
