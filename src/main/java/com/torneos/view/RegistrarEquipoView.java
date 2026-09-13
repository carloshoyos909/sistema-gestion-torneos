package com.torneos.view;

import com.torneos.model.Equipo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import java.util.HashSet;
import java.util.Set;

/**
 * UI para el registro de equipos y su respectiva nómina.
 * Cumple con REQ-FUNC-002 y REQ-FUNC-003[cite: 2].
 */
@Route("registrar-equipo")
public class RegistrarEquipoView extends VerticalLayout {
    
    private static final Set<String> equiposRegistrados = new HashSet<>();

    public RegistrarEquipoView() {
        TextField nombreEquipo = new TextField("Nombre del Equipo");
        nombreEquipo.setRequired(true);
        
        TextArea nominaJugadores = new TextArea("Nómina de Jugadores (separados por salto de línea)");
        nominaJugadores.setWidthFull();

        Button btnInscribir = new Button("Inscribir Equipo", event -> {
            String nombre = nombreEquipo.getValue().trim();
            
            if (equiposRegistrados.contains(nombre.toLowerCase())) {
                Notification.show("Error: El nombre del equipo ya se encuentra registrado.");
            } else if (!nombre.isEmpty()) {
                equiposRegistrados.add(nombre.toLowerCase());
                Equipo equipo = new Equipo(nombre);
                String[] listaJugadores = nominaJugadores.getValue().split("\n");
                
                for (String jug : listaJugadores) {
                    if (!jug.isBlank()) {
                        equipo.agregarJugador(jug.trim());
                    }
                }
                
                Notification.show("Éxito: Equipo y " + equipo.getJugadores().size() + " jugadores registrados.");
                nombreEquipo.clear();
                nominaJugadores.clear();
            }
        });

        add(nombreEquipo, nominaJugadores, btnInscribir);
    }
}