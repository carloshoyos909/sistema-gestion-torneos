package com.torneos.view;

import com.torneos.model.Torneo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Interfaz de Usuario (UI) para la creación de un nuevo torneo.
 * Cumple con el requisito REQ-FUNC-001 de la especificación SRS[cite: 2].
 * 
 * @author Santiago Bermúdez, Carlos Hoyos G., Miguel
 */
@Route("crear-torneo")
public class CrearTorneoView extends VerticalLayout {

    public CrearTorneoView() {
        TextField nombreTorneo = new TextField("Nombre de la Competencia");
        nombreTorneo.setRequired(true);
        
        ComboBox<String> deporte = new ComboBox<>("Deporte");
        deporte.setItems("Fútbol", "Baloncesto", "Voleibol");
        deporte.setRequired(true);
        
        TextField categoria = new TextField("Categoría");
        categoria.setRequiredIndicatorVisible(true);

        Button btnGuardar = new Button("Crear Torneo", event -> {
            if (!nombreTorneo.isEmpty() && deporte.getValue() != null && !categoria.isEmpty()) {
                Torneo nuevoTorneo = new Torneo(nombreTorneo.getValue(), deporte.getValue(), categoria.getValue());
                Notification.show("Torneo '" + nuevoTorneo.getNombre() + "' creado exitosamente en estado activo.");
                nombreTorneo.clear();
                categoria.clear();
                deporte.clear();
            } else {
                Notification.show("Error: Faltan datos requeridos.");
            }
        });

        add(nombreTorneo, deporte, categoria, btnGuardar);
    }
}