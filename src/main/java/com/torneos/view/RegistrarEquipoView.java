package com.torneos.view;

import com.torneos.model.Equipo;
import com.torneos.service.EquipoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "registrar-equipo", layout = MainView.class)
@PageTitle("Registrar Equipo")
public class RegistrarEquipoView extends VerticalLayout {

    private final EquipoService equipoService;

    public RegistrarEquipoView(EquipoService equipoService) {
        this.equipoService = equipoService;

        TextField nombreEquipo = new TextField("Nombre del Equipo");
        nombreEquipo.setRequired(true);
        nombreEquipo.setWidthFull();

        TextArea nominaJugadores = new TextArea("Nómina de Jugadores (uno por línea)");
        nominaJugadores.setWidthFull();
        nominaJugadores.setMinHeight("120px");

        Button btnInscribir = new Button("Inscribir Equipo", event -> {
            String nombre = nombreEquipo.getValue().trim();

            if (nombre.isEmpty()) {
                Notification n = Notification.show("El nombre del equipo es obligatorio.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                Equipo equipo = equipoService.registrarEquipo(nombre);

                String[] lineas = nominaJugadores.getValue().split("\n");
                for (String linea : lineas) {
                    if (!linea.isBlank()) {
                        equipo.agregarJugador(linea.trim());
                    }
                }

                Notification n = Notification.show(
                        "Éxito: Equipo \"" + equipo.getNombre() + "\" registrado con "
                        + equipo.getJugadores().size() + " jugador(es).");
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                nombreEquipo.clear();
                nominaJugadores.clear();

            } catch (IllegalArgumentException ex) {
                Notification n = Notification.show(ex.getMessage());
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        add(nombreEquipo, nominaJugadores, btnInscribir);
        setWidth("500px");
        getStyle().set("margin", "0 auto");
    }
}
