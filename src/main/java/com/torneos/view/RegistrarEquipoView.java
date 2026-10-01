package com.torneos.view;

import com.torneos.dto.EquipoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EquipoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route(value = "registrar-equipo", layout = MainView.class)
@PageTitle("Registrar Equipo")
public class RegistrarEquipoView extends VerticalLayout {

    private final EquipoService equipoService;
    private final TorneoService torneoService;
    private final ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");

    public RegistrarEquipoView(EquipoService equipoService, TorneoService torneoService) {
        this.equipoService = equipoService;
        this.torneoService = torneoService;

        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setRequired(true);
        torneo.setWidthFull();
        cargarTorneos();

        TextField nombreEquipo = new TextField("Nombre del Equipo");
        nombreEquipo.setRequired(true);
        nombreEquipo.setWidthFull();

        TextArea nominaJugadores = new TextArea("Nómina de Jugadores (uno por línea)");
        nominaJugadores.setWidthFull();
        nominaJugadores.setMinHeight("120px");

        Button btnInscribir = new Button("Inscribir Equipo", event -> {
            if (torneo.getValue() == null) {
                mostrarError("Debe seleccionar un torneo.");
                return;
            }

            String nombre = nombreEquipo.getValue().trim();
            if (nombre.isEmpty()) {
                mostrarError("El nombre del equipo es obligatorio.");
                return;
            }

            List<String> jugadores = Arrays.stream(nominaJugadores.getValue().split("\\R"))
                    .map(String::trim)
                    .filter(linea -> !linea.isBlank())
                    .distinct()
                    .toList();

            try {
                EquipoResponse equipo = equipoService.registrarEquipo(
                        torneo.getValue().id(),
                        nombre,
                        jugadores
                );

                Notification n = Notification.show(
                        "Equipo \"" + equipo.nombre() + "\" registrado con "
                                + equipo.jugadores().size() + " jugador(es).");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                nombreEquipo.clear();
                nominaJugadores.clear();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });

        add(torneo, nombreEquipo, nominaJugadores, btnInscribir);
        setWidth("500px");
        getStyle().set("margin", "0 auto");
    }

    private void cargarTorneos() {
        torneo.setItems(torneoService.getTorneosActivos());
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show("Error: " + mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
