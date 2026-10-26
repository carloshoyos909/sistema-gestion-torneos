package com.torneos.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "jugador", layout = MainView.class)
@PageTitle("Panel Jugador")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class JugadorView extends VerticalLayout {

    public JugadorView() {
        H2 titulo = new H2("⚽ Panel de Jugador");
        Paragraph desc = new Paragraph(
                "Consulta los equipos y el fixture de tus torneos.");

        add(titulo, desc);
        setPadding(true);
        setSpacing(true);
    }
}
