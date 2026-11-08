package com.torneos.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Iniciar Sesión | Sistema de Torneos")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        login.setAction("login");

        Anchor linkRegistro = new Anchor("registro", "¿Eres organizador? Regístrate aquí");
        linkRegistro.getStyle().set("font-size", "var(--lumo-font-size-s)");

        com.vaadin.flow.component.html.Paragraph hint = new com.vaadin.flow.component.html.Paragraph(
                "Jugadores: tu usuario es tu nombre sin espacios en minúsculas (ej. 'Carlos Hoyos' → 'carloshoyos') y tu contraseña es 12345");
        hint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("max-width", "360px")
                .set("text-align", "center");

        add(new H1("🏆 Sistema de Torneos"), login, linkRegistro, hint);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}
