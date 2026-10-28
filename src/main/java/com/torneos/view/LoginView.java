package com.torneos.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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

        Paragraph hint = new Paragraph("Si eres jugador, tu usuario es tu nombre completo en minúsculas, "
                + "sin espacios ni caracteres especiales (ej.: Miguel Ángel Pérez → miguelangelperez). "
                + "La contraseña inicial es 12345; después de iniciar sesión puedes cambiarla desde Mi Perfil.");
        hint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("max-width", "500px")
                .set("text-align", "center");

        Anchor linkRegistro = new Anchor("registro", "¿Eres organizador? Regístrate aquí");
        linkRegistro.getStyle().set("font-size", "var(--lumo-font-size-s)");

        add(new H1("🏆 Sistema de Torneos"), login, hint, linkRegistro);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}
