package com.torneos.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "organizador", layout = MainView.class)
@PageTitle("Panel Organizador")
@RolesAllowed({"ADMIN", "ORGANIZADOR"})
public class OrganizadorView extends VerticalLayout {

    public OrganizadorView() {
        H2 titulo = new H2("📋 Panel de Organizador");
        Paragraph desc = new Paragraph(
                "Desde aquí puedes gestionar torneos, registrar equipos y programar partidos.");

        add(titulo, desc);
        setPadding(true);
        setSpacing(true);
    }
}
