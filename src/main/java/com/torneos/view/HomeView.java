package com.torneos.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "", layout = MainView.class)
@PageTitle("Inicio — Gestión de Torneos")
@RolesAllowed({"ADMIN", "ORGANIZADOR", "JUGADOR"})
public class HomeView extends VerticalLayout {

    public HomeView() {
        boolean esAdmin       = tieneRol("ROLE_ADMIN");
        boolean esOrganizador = tieneRol("ROLE_ORGANIZADOR");

        H2 bienvenida = new H2("Bienvenido al Sistema de Gestión de Torneos TorneON");
        bienvenida.getStyle().set("margin-bottom", "4px");

        Paragraph descripcion = new Paragraph(
                "Usa el menú lateral o las tarjetas de abajo para navegar.");
        descripcion.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "0");

        // Grid de tarjetas con FlexLayout (wrap automático en 2-3 columnas)
        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getStyle().set("gap", "20px").set("padding", "8px 0");
        grid.setWidthFull();

        if (esAdmin || esOrganizador) {
            grid.add(crearTarjeta(VaadinIcon.TROPHY,     "Crear Torneo",
                    "Registra una nueva competencia con nombre, deporte y categoría.", "crear-torneo", "#fff8e1", "#e65100"));
            grid.add(crearTarjeta(VaadinIcon.USERS,      "Registrar Equipo",
                    "Inscribe un equipo y agrega su nómina de jugadores.", "registrar-equipo", "#e8eaf6", "#1a237e"));
            grid.add(crearTarjeta(VaadinIcon.CALENDAR,   "Programar Partido",
                    "Asigna fechas, horas y canchas a los encuentros del fixture.", "programar-partido", "#e8f5e9", "#2e7d32"));
            grid.add(crearTarjeta(VaadinIcon.CLIPBOARD,  "Panel Organizador",
                    "Genera el fixture automáticamente según el formato del torneo.", "organizador", "#f3e5f5", "#6a1b9a"));
        }

        grid.add(crearTarjeta(VaadinIcon.GROUP,          "Consultar Equipos",
                "Consulta los equipos inscritos en cada torneo.", "equipos", "#e8eaf6", "#1a237e"));
        grid.add(crearTarjeta(VaadinIcon.LIST,           "Consultar Fixture",
                "Revisa el calendario de partidos programados.", "fixture", "#e0f2f1", "#00695c"));
        grid.add(crearTarjeta(VaadinIcon.MEDAL,          "Tabla de Posiciones",
                "Consulta la clasificación actualizada del torneo.", "tabla-posiciones", "#fff8e1", "#e65100"));
        grid.add(crearTarjeta(VaadinIcon.CHART,          "Panel Jugador",
                "Consulta y gestiona estadísticas de jugadores.", "jugador", "#e8f5e9", "#2e7d32"));

        if (esAdmin) {
            grid.add(crearTarjeta(VaadinIcon.COG,        "Administración",
                    "Gestiona los usuarios del sistema.", "admin", "#fdecea", "#b71c1c"));
        }

        setAlignItems(FlexComponent.Alignment.START);
        setPadding(true);
        setSpacing(true);
        add(bienvenida, descripcion, grid);
    }

    private VerticalLayout crearTarjeta(VaadinIcon vaadinIcon, String titulo,
                                         String descripcion, String ruta,
                                         String iconBg, String iconColor) {
        // Ícono con fondo circular
        Icon icon = new Icon(vaadinIcon);
        icon.getStyle()
                .set("color", iconColor)
                .set("width", "24px")
                .set("height", "24px");

        Span iconWrapper = new Span(icon);
        iconWrapper.getStyle()
                .set("background", iconBg)
                .set("border-radius", "50%")
                .set("padding", "10px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "48px")
                .set("height", "48px")
                .set("flex-shrink", "0");

        H3 tituloComp = new H3(titulo);
        tituloComp.getStyle()
                .set("margin", "0")
                .set("color", "#1a237e")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600");

        Paragraph descComp = new Paragraph(descripcion);
        descComp.getStyle()
                .set("margin", "4px 0 12px 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.85rem");

        Span flecha = new Span("Ir →");
        flecha.getStyle()
                .set("color", iconColor)
                .set("font-weight", "600")
                .set("font-size", "0.85rem");

        VerticalLayout tarjeta = new VerticalLayout(iconWrapper, tituloComp, descComp, flecha);
        tarjeta.addClassName("tarjeta-home");
        tarjeta.setPadding(true);
        tarjeta.setSpacing(false);

        // Toda la tarjeta es cliqueable
        tarjeta.addClickListener(e -> UI.getCurrent().navigate(ruta));
        tarjeta.getStyle().set("cursor", "pointer");

        return tarjeta;
    }

    private boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }
}
