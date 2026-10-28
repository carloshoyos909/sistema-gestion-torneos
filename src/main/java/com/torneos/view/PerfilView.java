package com.torneos.view;

import com.torneos.model.Usuario;
import com.torneos.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "perfil", layout = MainView.class)
@PageTitle("Mi Perfil")
@PermitAll
public class PerfilView extends VerticalLayout {

    private final UsuarioService usuarioService;

    public PerfilView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;

        String username = obtenerUsername();
        Usuario usuario = usuarioService.buscarPorUsername(username);

        H2 titulo = new H2("👤 Mi Perfil");

        TextField campoUsuario = new TextField("Nombre de usuario");
        campoUsuario.setValue(usuario.getUsername());
        campoUsuario.setReadOnly(true);
        campoUsuario.setWidthFull();

        TextField campoRol = new TextField("Rol asignado");
        campoRol.setValue(usuario.getRol().name());
        campoRol.setReadOnly(true);
        campoRol.setWidthFull();

        Paragraph notaRol = new Paragraph("El rol solo puede ser modificado por un administrador.");
        notaRol.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        H3 tituloPassword = new H3("Cambiar contraseña");

        PasswordField passwordActual = new PasswordField("Contraseña actual");
        passwordActual.setWidthFull();

        PasswordField passwordNueva = new PasswordField("Nueva contraseña");
        passwordNueva.setWidthFull();
        passwordNueva.setHelperText("Mínimo 4 caracteres.");

        PasswordField passwordConfirmar = new PasswordField("Confirmar nueva contraseña");
        passwordConfirmar.setWidthFull();

        Button btnGuardar = new Button("Actualizar contraseña", e -> {
            String actual = passwordActual.getValue();
            String nueva = passwordNueva.getValue();
            String confirmar = passwordConfirmar.getValue();

            if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) {
                mostrarError("Completa todos los campos de contraseña.");
                return;
            }

            if (!nueva.equals(confirmar)) {
                mostrarError("La nueva contraseña y su confirmación no coinciden.");
                return;
            }

            try {
                usuarioService.cambiarPassword(username, actual, nueva);
                mostrarExito("Contraseña actualizada correctamente.");
                passwordActual.clear();
                passwordNueva.clear();
                passwordConfirmar.clear();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(titulo, campoUsuario, campoRol, notaRol, tituloPassword,
                passwordActual, passwordNueva, passwordConfirmar, btnGuardar);
        setWidth("500px");
        getStyle().set("margin", "0 auto");
        setPadding(true);
        setSpacing(true);
    }

    private String obtenerUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }

    private void mostrarExito(String mensaje) {
        Notification n = Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void mostrarError(String mensaje) {
        Notification n = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
