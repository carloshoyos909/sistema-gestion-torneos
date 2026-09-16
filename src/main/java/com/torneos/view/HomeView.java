package com.torneos.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainView.class)
@PageTitle("Inicio — Gestión de Torneos")
public class HomeView extends VerticalLayout {

    public HomeView() {
        H2 bienvenida = new H2("Bienvenido al Sistema de Gestión de Torneos");

        Paragraph descripcion = new Paragraph(
                "Usa el menú lateral o las tarjetas de abajo para navegar.");

        VerticalLayout tarjetas = new VerticalLayout(
                crearTarjeta("🏆 Crear Torneo",
                        "Registra una nueva competencia con nombre, deporte y categoría.",
                        "crear-torneo"),
                crearTarjeta("👥 Registrar Equipo",
                        "Inscribe un equipo y agrega su nómina de jugadores.",
                        "registrar-equipo"),
                crearTarjeta("📅 Programar Partido",
                        "Asigna fechas, horas y canchas a los encuentros del fixture.",
                        "programar-partido")
        );
        tarjetas.setSpacing(true);
        tarjetas.setPadding(false);

        setAlignItems(FlexComponent.Alignment.START);
        setPadding(true);
        setSpacing(true);
        add(bienvenida, descripcion, tarjetas);
    }

    private VerticalLayout crearTarjeta(String titulo, String descripcion, String ruta) {
        H3 tituloComp = new H3(titulo);
        tituloComp.getStyle().set("margin", "0");

        Paragraph descComp = new Paragraph(descripcion);
        descComp.getStyle()
                .set("margin", "4px 0 8px 0")
                .set("color", "var(--lumo-secondary-text-color)");

        Anchor enlace = new Anchor(ruta, "Ir →");

        VerticalLayout tarjeta = new VerticalLayout(tituloComp, descComp, enlace);
        tarjeta.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-base-color)")
                .set("max-width", "420px");
        tarjeta.setPadding(true);
        return tarjeta;
    }
}
