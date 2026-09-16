package com.torneos.view;

import com.torneos.model.Torneo;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "crear-torneo", layout = MainView.class)
@PageTitle("Crear Torneo")
public class CrearTorneoView extends VerticalLayout {

    private final TorneoService torneoService;

    public CrearTorneoView(TorneoService torneoService) {
        this.torneoService = torneoService;

        TextField nombreTorneo = new TextField("Nombre de la Competencia");
        nombreTorneo.setRequired(true);
        nombreTorneo.setWidthFull();

        ComboBox<String> deporte = new ComboBox<>("Deporte");
        deporte.setItems("Fútbol", "Baloncesto", "Voleibol");
        deporte.setRequired(true);
        deporte.setWidthFull();

        TextField categoria = new TextField("Categoría");
        categoria.setRequiredIndicatorVisible(true);
        categoria.setWidthFull();

        Button btnGuardar = new Button("Crear Torneo", event -> {
            if (nombreTorneo.isEmpty() || deporte.getValue() == null || categoria.isEmpty()) {
                Notification n = Notification.show("Error: Faltan datos requeridos.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Torneo nuevo = torneoService.crearTorneo(
                    nombreTorneo.getValue(),
                    deporte.getValue(),
                    categoria.getValue()
            );

            Notification n = Notification.show(
                    "Torneo \"" + nuevo.getNombre() + "\" creado exitosamente en estado activo.");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nombreTorneo.clear();
            categoria.clear();
            deporte.clear();
        });

        add(nombreTorneo, deporte, categoria, btnGuardar);
        setWidth("500px");
        getStyle().set("margin", "0 auto");
    }
}
