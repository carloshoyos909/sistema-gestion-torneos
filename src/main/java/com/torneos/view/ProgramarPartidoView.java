package com.torneos.view;

import com.torneos.dto.EquipoResponse;
import com.torneos.dto.PartidoResponse;
import com.torneos.dto.TorneoResponse;
import com.torneos.service.EquipoService;
import com.torneos.service.PartidoService;
import com.torneos.service.TorneoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "programar-partido", layout = MainView.class)
@PageTitle("Programar Partido")
public class ProgramarPartidoView extends VerticalLayout {

    private final TorneoService torneoService;
    private final EquipoService equipoService;
    private final PartidoService partidoService;

    private final ComboBox<TorneoResponse> torneo = new ComboBox<>("Torneo");
    private final ComboBox<EquipoResponse> equipoLocal = new ComboBox<>("Equipo Local");
    private final ComboBox<EquipoResponse> equipoVisitante = new ComboBox<>("Equipo Visitante");

    public ProgramarPartidoView(TorneoService torneoService,
                                EquipoService equipoService,
                                PartidoService partidoService) {
        this.torneoService = torneoService;
        this.equipoService = equipoService;
        this.partidoService = partidoService;

        torneo.setItemLabelGenerator(TorneoResponse::nombre);
        torneo.setRequired(true);
        torneo.setWidthFull();
        torneo.setItems(torneoService.getTorneosActivos());
        torneo.addValueChangeListener(event -> cargarEquipos());

        equipoLocal.setItemLabelGenerator(EquipoResponse::nombre);
        equipoVisitante.setItemLabelGenerator(EquipoResponse::nombre);
        equipoLocal.setRequired(true);
        equipoVisitante.setRequired(true);

        DatePicker fecha = new DatePicker("Fecha del Encuentro");
        TimePicker hora = new TimePicker("Hora");
        TextField cancha = new TextField("Cancha / Lugar");
        cancha.setRequired(true);

        HorizontalLayout equiposLayout = new HorizontalLayout(equipoLocal, equipoVisitante);
        HorizontalLayout fechaLugarLayout = new HorizontalLayout(fecha, hora, cancha);
        equiposLayout.setWidthFull();
        fechaLugarLayout.setWidthFull();

        Button btnProgramar = new Button("Programar Partido", event -> {
            if (torneo.getValue() == null || equipoLocal.getValue() == null
                    || equipoVisitante.getValue() == null || fecha.getValue() == null
                    || hora.getValue() == null || cancha.isEmpty()) {
                mostrarError("Complete todos los parámetros de programación.");
                return;
            }

            try {
                PartidoResponse nuevoPartido = partidoService.programarPartido(
                        torneo.getValue().id(),
                        equipoLocal.getValue().id(),
                        equipoVisitante.getValue().id(),
                        fecha.getValue(),
                        hora.getValue(),
                        cancha.getValue()
                );

                Notification n = Notification.show(
                        "Partido programado: " + nuevoPartido.equipoLocal()
                                + " vs " + nuevoPartido.equipoVisitante()
                                + " — " + nuevoPartido.fecha() + " " + nuevoPartido.hora()
                                + " en " + nuevoPartido.cancha());
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                equipoLocal.clear();
                equipoVisitante.clear();
                fecha.clear();
                hora.clear();
                cancha.clear();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });

        add(torneo, equiposLayout, fechaLugarLayout, btnProgramar);
        setWidth("700px");
        getStyle().set("margin", "0 auto");
    }

    private void cargarEquipos() {
        equipoLocal.clear();
        equipoVisitante.clear();

        if (torneo.getValue() == null) {
            equipoLocal.setItems();
            equipoVisitante.setItems();
            return;
        }

        var equipos = equipoService.getEquipos(torneo.getValue().id());
        equipoLocal.setItems(equipos);
        equipoVisitante.setItems(equipos);
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show("Error: " + mensaje);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
