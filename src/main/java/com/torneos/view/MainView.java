package com.torneos.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServletRequest;

import jakarta.annotation.security.PermitAll;

@PermitAll
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

        String username = obtenerUsername();
        Span usuarioSpan = new Span("👤 " + username);
        usuarioSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Button btnLogout = new Button("Cerrar sesión", e -> cerrarSesion());
        btnLogout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        HorizontalLayout encabezado = new HorizontalLayout(toggle, titulo);
        encabezado.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        encabezado.setWidthFull();
        encabezado.addClassNames("py-0", "px-m");

        HorizontalLayout derecha = new HorizontalLayout(usuarioSpan, btnLogout);
        derecha.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        derecha.getStyle().set("margin-left", "auto").set("margin-right", "var(--lumo-space-m)");

        HorizontalLayout navbar = new HorizontalLayout(encabezado, derecha);
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        addToNavbar(navbar);
    }

    private void cerrarSesion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    null,
                    auth
            );
        }
        getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
    }

    private void crearNavegacionLateral() {
        SideNav nav = new SideNav();

        boolean esAdmin = tieneRol("ROLE_ADMIN");
        boolean esOrganizador = tieneRol("ROLE_ORGANIZADOR");

        nav.addItem(new SideNavItem("🏠 Inicio", HomeView.class));

        if (esAdmin || esOrganizador) {
            nav.addItem(new SideNavItem("🏆 Crear Torneo",      CrearTorneoView.class));
            nav.addItem(new SideNavItem("👥 Registrar Equipo",  RegistrarEquipoView.class));
            nav.addItem(new SideNavItem("📅 Programar Partido", ProgramarPartidoView.class));
            nav.addItem(new SideNavItem("📋 Panel Organizador", OrganizadorView.class));
        }

        nav.addItem(new SideNavItem("👥 Consultar Equipos",   EquiposView.class));
        nav.addItem(new SideNavItem("📋 Consultar Fixture",    FixtureView.class));
        nav.addItem(new SideNavItem("🥇 Tabla de Posiciones",  TablaPosicionesView.class));
        nav.addItem(new SideNavItem("⚽ Panel Jugador",        JugadorView.class));
        nav.addItem(new SideNavItem("👤 Mi Perfil",            PerfilView.class));

        if (esAdmin) {
            nav.addItem(new SideNavItem("⚙️ Administración", AdminView.class));
        }

        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent.setSizeUndefined();
        addToDrawer(drawerContent);
    }

    private boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    private String obtenerUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "Invitado";
        return auth.getName();
    }
}
