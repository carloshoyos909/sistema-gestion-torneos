package com.torneos.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;

public class MainView extends AppLayout implements RouterLayout {

    public MainView() {
        crearEncabezado();
        crearNavegacionLateral();
    }

    private void crearEncabezado() {
        DrawerToggle toggle = new DrawerToggle();

        H1 titulo = new H1("Gestión de Torneos");
        titulo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        HorizontalLayout encabezado = new HorizontalLayout(toggle, titulo);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.setWidthFull();
        encabezado.addClassNames("py-0", "px-m");

        addToNavbar(encabezado);
    }

    private void crearNavegacionLateral() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("🏠 Inicio",            HomeView.class));
        nav.addItem(new SideNavItem("🏆 Crear Torneo",      CrearTorneoView.class));
        nav.addItem(new SideNavItem("👥 Registrar Equipo",  RegistrarEquipoView.class));
        nav.addItem(new SideNavItem("📅 Programar Partido", ProgramarPartidoView.class));

        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent.setSizeUndefined();
        addToDrawer(drawerContent);
    }
}
