package com.torneos.view;

import com.torneos.model.Rol;
import com.torneos.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("registro")
@PageTitle("Registro de Organizador")
@AnonymousAllowed
public class RegistroView extends VerticalLayout {

    public RegistroView(UsuarioService usuarioService) {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout formulario = new VerticalLayout();
        formulario.setWidth("400px");
        formulario.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)");

        H2 titulo = new H2("📋 Registro de Organizador");
        titulo.getStyle().set("margin-top", "0");

        Paragraph desc = new Paragraph("Crea tu cuenta para gestionar torneos y equipos.");
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "0");

        TextField username = new TextField("Nombre de usuario");
        username.setWidthFull();
        username.setRequired(true);
        username.setHelperText("Mínimo 3 caracteres, sin espacios.");

        PasswordField password = new PasswordField("Contraseña");
        password.setWidthFull();
        password.setRequired(true);
        password.setHelperText("Mínimo 4 caracteres.");

        PasswordField confirmar = new PasswordField("Confirmar contraseña");
        confirmar.setWidthFull();
        confirmar.setRequired(true);

        Button btnRegistrar = new Button("Crear cuenta de Organizador", e -> {
            String user = username.getValue().trim();
            String pass = password.getValue();
            String conf = confirmar.getValue();

            if (user.isBlank() || user.length() < 3) {
                mostrarError("El nombre de usuario debe tener al menos 3 caracteres.");
                return;
            }
            if (user.contains(" ")) {
                mostrarError("El nombre de usuario no puede tener espacios.");
                return;
            }
            if (pass.isBlank()) {
                mostrarError("La contraseña es obligatoria.");
                return;
            }
            if (!pass.equals(conf)) {
                mostrarError("Las contraseñas no coinciden.");
                return;
            }

            try {
                usuarioService.registrarUsuario(user, pass, Rol.ORGANIZADOR);
                Notification n = Notification.show(
                        "Cuenta de Organizador \"" + user + "\" creada. Ya puedes iniciar sesión.",
                        4000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                username.clear();
                password.clear();
                confirmar.clear();

                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnRegistrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnRegistrar.setWidthFull();

        Anchor linkLogin = new Anchor("login", "¿Ya tienes cuenta? Inicia sesión aquí");
        linkLogin.getStyle().set("font-size", "var(--lumo-font-size-s)");

        formulario.add(titulo, desc, username, password, confirmar, btnRegistrar, linkLogin);
        add(formulario);
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
