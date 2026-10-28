package com.torneos.view;

import com.torneos.dto.TorneoResponse;
import com.torneos.model.FormatoTorneo;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "crear-torneo", layout = MainView.class)
@PageTitle("Crear Torneo")
@RolesAllowed({"ADMIN", "ORGANIZADOR"})
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
        categoria.setRequired(true);
        categoria.setWidthFull();

        ComboBox<FormatoTorneo> formato = new ComboBox<>("Formato de Competición");
        formato.setItems(FormatoTorneo.values());
        formato.setItemLabelGenerator(FormatoTorneo::getLabel);
        formato.setValue(FormatoTorneo.LIGA);
        formato.setRequired(true);
        formato.setWidthFull();

        Button btnGuardar = new Button("Crear Torneo", event -> {
            if (nombreTorneo.isEmpty() || deporte.getValue() == null
                    || categoria.isEmpty() || formato.getValue() == null) {
                mostrarError("Faltan datos requeridos.");
                return;
            }
            try {
                TorneoResponse nuevo = torneoService.crearTorneo(
                        nombreTorneo.getValue(),
                        deporte.getValue(),
                        categoria.getValue(),
                        formato.getValue()
                );
                Notification n = Notification.show(
                        "Torneo \"" + nuevo.nombre() + "\" creado — Formato: " + nuevo.formato().getLabel());
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                nombreTorneo.clear();
                categoria.clear();
                deporte.clear();
                formato.setValue(FormatoTorneo.LIGA);
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });

        add(nombreTorneo, deporte, categoria, formato, btnGuardar);
        setWidth("500px");
        getStyle().set("margin", "0 auto");
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show("Error: " + mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
