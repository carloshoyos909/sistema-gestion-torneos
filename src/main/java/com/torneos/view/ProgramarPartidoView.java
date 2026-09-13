package com.torneos.view;

import com.torneos.model.Partido;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

/**
 * UI para la gestión del calendario de la competencia (Fixture).
 * Cumple con el requisito REQ-FUNC-004[cite: 2].
 */
@Route("programar-partido")
public class ProgramarPartidoView extends VerticalLayout {

    public ProgramarPartidoView() {
        ComboBox<String> equipoLocal = new ComboBox<>("Equipo Local");
        ComboBox<String> equipoVisitante = new ComboBox<>("Equipo Visitante");
        
        equipoLocal.setItems("Equipo A", "Equipo B", "Equipo C");
        equipoVisitante.setItems("Equipo A", "Equipo B", "Equipo C");

        DatePicker fecha = new DatePicker("Fecha del Encuentro");
        TimePicker hora = new TimePicker("Hora");
        TextField cancha = new TextField("Cancha / Lugar");

        HorizontalLayout equiposLayout = new HorizontalLayout(equipoLocal, equipoVisitante);
        HorizontalLayout fechaLugarLayout = new HorizontalLayout(fecha, hora, cancha);

        Button btnProgramar = new Button("Programar Partido", event -> {
            String local = equipoLocal.getValue();
            String visitante = equipoVisitante.getValue();
            
            if (local != null && local.equals(visitante)) {
                Notification.show("Conflicto: Un equipo no puede jugar contra sí mismo.");
            } else if (local != null && visitante != null && fecha.getValue() != null && hora.getValue() != null) {
                Partido nuevoPartido = new Partido(local, visitante, fecha.getValue(), hora.getValue(), cancha.getValue());
                Notification.show("Encuentro programado exitosamente en el fixture.");
            } else {
                Notification.show("Complete todos los parámetros de programación.");
            }
        });

        add(equiposLayout, fechaLugarLayout, btnProgramar);
    }
}