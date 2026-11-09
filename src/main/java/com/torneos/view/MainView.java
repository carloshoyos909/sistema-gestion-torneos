package com.torneos.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@PermitAll
public class MainView extends AppLayout implements RouterLayout {

    public MainView() {
        crearEncabezado();
        crearNavegacionLateral();
    }

    private void crearEncabezado() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle().set("color", "#ffffff");

        H1 titulo = new H1("TorneON");
        titulo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0")
                .set("color", "#ffffff")
                .set("font-weight", "700");

        titulo.addClassName("header-titulo");

        HorizontalLayout izquierda = new HorizontalLayout(toggle, titulo);
        izquierda.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        izquierda.setWidthFull();
        izquierda.addClassNames("py-0", "px-m");

        String username = obtenerUsername();
        String rol = obtenerRol();

        Avatar avatar = new Avatar(username);
        avatar.setColorIndex(rol.equals("ADMIN") ? 1 : rol.equals("ORGANIZADOR") ? 2 : 3);

        Span badgeRol = new Span(rol);
        badgeRol.getElement().getThemeList().add("badge " + (
                rol.equals("ADMIN") ? "error" :
                rol.equals("ORGANIZADOR") ? "contrast" : "success"));
        badgeRol.getStyle().set("font-size", "0.7rem").set("font-weight", "600");

        Span nombreSpan = new Span(username);
        nombreSpan.getStyle()
                .set("color", "#ffffff")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        VerticalLayout infoUsuario = new VerticalLayout(nombreSpan, badgeRol);
        infoUsuario.setSpacing(false);
        infoUsuario.setPadding(false);

        HorizontalLayout derecha = new HorizontalLayout(avatar, infoUsuario);
        derecha.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        derecha.setSpacing(true);
        derecha.getStyle().set("margin-left", "auto").set("margin-right", "var(--lumo-space-m)");

        derecha.addClassName("perfil-usuario");

        HorizontalLayout navbar = new HorizontalLayout(izquierda, derecha);
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        addToNavbar(navbar);
    }

    private void crearNavegacionLateral() {
        boolean esAdmin       = tieneRol("ROLE_ADMIN");
        boolean esOrganizador = tieneRol("ROLE_ORGANIZADOR");

        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.getStyle().set("padding", "8px");

        // ── Inicio ────────────────────────────────────────────────────────────
        SideNav navInicio = new SideNav();
        navInicio.addItem(new SideNavItem("🏠 Inicio", HomeView.class));
        drawerContent.add(navInicio);

        // ── Sección: Gestión ─────────────────────────────────────────────────
        if (esAdmin || esOrganizador) {
            drawerContent.add(crearSeccion("GESTIÓN"));
            SideNav navGestion = new SideNav();
            navGestion.addItem(new SideNavItem("🏆 Crear Torneo",       CrearTorneoView.class));
            navGestion.addItem(new SideNavItem("👥 Registrar Equipo",   RegistrarEquipoView.class));
            navGestion.addItem(new SideNavItem("📅 Programar Partido",  ProgramarPartidoView.class));
            navGestion.addItem(new SideNavItem("📋 Panel Organizador",  OrganizadorView.class));
            drawerContent.add(navGestion);
        }

        // ── Sección: Consultas ───────────────────────────────────────────────
        drawerContent.add(crearSeccion("CONSULTAS"));
        SideNav navConsultas = new SideNav();
        navConsultas.addItem(new SideNavItem("👥 Consultar Equipos",    EquiposView.class));
        navConsultas.addItem(new SideNavItem("📋 Consultar Fixture",    FixtureView.class));
        navConsultas.addItem(new SideNavItem("🥇 Tabla de Posiciones",  TablaPosicionesView.class));
        navConsultas.addItem(new SideNavItem("⚽ Panel Jugador",        JugadorView.class));
        drawerContent.add(navConsultas);

        // ── Sección: Sistema ─────────────────────────────────────────────────
        drawerContent.add(crearSeccion("SISTEMA"));
        SideNav navSistema = new SideNav();
        navSistema.addItem(new SideNavItem("👤 Mi Perfil", PerfilView.class));
        if (esAdmin) {
            navSistema.addItem(new SideNavItem("⚙️ Administración", AdminView.class));
        }
        drawerContent.add(navSistema);

        // ── Logout al fondo ───────────────────────────────────────────────────
        VerticalLayout spacer = new VerticalLayout();
        spacer.setFlexGrow(1, spacer);
        drawerContent.add(spacer);

        Hr separador = new Hr();
        separador.getStyle().set("margin", "8px 0");

        Button btnLogout = new Button("Cerrar sesión", new Icon(VaadinIcon.SIGN_OUT), e -> cerrarSesion());
        btnLogout.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnLogout.setWidthFull();

        drawerContent.add(separador, btnLogout);
        addToDrawer(drawerContent);
    }

    private Span crearSeccion(String label) {
        Span seccion = new Span(label);
        seccion.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.08em")
                .set("color", "rgba(255,255,255,0.45)")
                .set("padding", "12px 12px 4px 12px")
                .set("display", "block");
        return seccion;
    }

    private void cerrarSesion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    null, auth);
        }
        getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
    }

    private boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(rol));
    }

    private String obtenerUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || auth.getName() == null) ? "Invitado" : auth.getName();
    }

    private String obtenerRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("");
    }
}
