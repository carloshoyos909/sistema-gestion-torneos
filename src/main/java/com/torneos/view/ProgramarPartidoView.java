package com.torneos.view;

import com.torneos.model.Partido;
import com.torneos.service.EquipoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "programar-partido", layout = MainView.class)
@PageTitle("Programar Partido")
public class ProgramarPartidoView extends VerticalLayout implements BeforeEnterObserver {

    private final EquipoService equipoService;

    private final ComboBox<String> equipoLocal = new ComboBox<>("Equipo Local");
    private final ComboBox<String> equipoVisitante = new ComboBox<>("Equipo Visitante");

    public ProgramarPartidoView(EquipoService equipoService) {
        this.equipoService = equipoService;

        DatePicker fecha = new DatePicker("Fecha del Encuentro");
        TimePicker hora = new TimePicker("Hora");
        TextField cancha = new TextField("Cancha / Lugar");
        cancha.setRequired(true);

        HorizontalLayout equiposLayout = new HorizontalLayout(equipoLocal, equipoVisitante);
        HorizontalLayout fechaLugarLayout = new HorizontalLayout(fecha, hora, cancha);

        Button btnProgramar = new Button("Programar Partido", event -> {
            String local = equipoLocal.getValue();
            String visitante = equipoVisitante.getValue();

            if (local == null || visitante == null || fecha.getValue() == null
                    || hora.getValue() == null || cancha.isEmpty()) {
                Notification n = Notification.show("Complete todos los parámetros de programación.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (local.equals(visitante)) {
                Notification n = Notification.show("Conflicto: Un equipo no puede jugar contra sí mismo.");
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Partido nuevoPartido = new Partido(local, visitante, fecha.getValue(), hora.getValue(), cancha.getValue());
            Notification n = Notification.show(
                    "Partido programado: " + nuevoPartido.getEquipoLocal()
                    + " vs " + nuevoPartido.getEquipoVisitante()
                    + " — " + nuevoPartido.getFecha() + " " + nuevoPartido.getHora()
                    + " en " + nuevoPartido.getCancha());
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            equipoLocal.clear();
            equipoVisitante.clear();
            fecha.clear();
            hora.clear();
            cancha.clear();
        });

        add(equiposLayout, fechaLugarLayout, btnProgramar);
        setWidth("600px");
        getStyle().set("margin", "0 auto");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> nombres = equipoService.getNombresEquipos();
        equipoLocal.setItems(nombres);
        equipoVisitante.setItems(nombres);
    }
}
