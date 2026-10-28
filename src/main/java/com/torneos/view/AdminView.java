package com.torneos.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.torneos.model.Rol;
import com.torneos.model.Usuario;
import com.torneos.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainView.class)
@PageTitle("Panel de Administración")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final Grid<Usuario> grid = new Grid<>(Usuario.class, false);

    public AdminView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;

        H2 titulo = new H2("⚙️ Panel de Administración");
        H3 subtitulo = new H3("Gestión de usuarios");

        configurarGrid();
        cargarUsuarios();

        Button btnNuevoUsuario = new Button("➕ Nuevo usuario", e -> abrirDialogoNuevoUsuario());
        btnNuevoUsuario.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(titulo, subtitulo, btnNuevoUsuario, grid);
        setPadding(true);
        setSpacing(true);
    }

    private void configurarGrid() {
        grid.addColumn(Usuario::getUsername).setHeader("Usuario").setSortable(true);
        grid.addColumn(u -> u.getRol().name()).setHeader("Rol").setSortable(true);

        grid.addComponentColumn(usuario -> {
            String usernameActual = obtenerUsernameActual();

            ComboBox<Rol> comboRol = new ComboBox<>();
            comboRol.setItems(Rol.values());
            comboRol.setValue(usuario.getRol());
            comboRol.setWidth("150px");

            boolean esMismoUsuario = usuario.getUsername().equals(usernameActual);
            comboRol.setEnabled(!esMismoUsuario);
            if (esMismoUsuario) {
                comboRol.setHelperText("No puedes cambiar tu propio rol.");
            }

            comboRol.addValueChangeListener(event -> {
                if (event.getValue() != null && event.getValue() != event.getOldValue()) {
                    try {
                        usuarioService.cambiarRol(usuario.getId(), usernameActual, event.getValue());
                        mostrarExito("Rol de " + usuario.getUsername() + " actualizado a " + event.getValue());
                        cargarUsuarios();
                    } catch (IllegalArgumentException ex) {
                        mostrarError(ex.getMessage());
                        comboRol.setValue(event.getOldValue());
                    }
                }
            });
            return comboRol;
        }).setHeader("Cambiar Rol");

        grid.addComponentColumn(usuario -> {
            String usernameActual = obtenerUsernameActual();
            Button btnEliminar = new Button("🗑️ Eliminar");
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            btnEliminar.setEnabled(!usuario.getUsername().equals(usernameActual));

            btnEliminar.addClickListener(e -> {
                Dialog confirmacion = new Dialog();
                confirmacion.setHeaderTitle("Confirmar eliminación");

                VerticalLayout content = new VerticalLayout();
                content.add("¿Eliminar al usuario \"" + usuario.getUsername() + "\"? Esta acción no se puede deshacer.");

                Button btnConfirmar = new Button("Eliminar", ev -> {
                    try {
                        usuarioService.eliminarUsuario(usuario.getId(), usernameActual);
                        mostrarExito("Usuario eliminado correctamente.");
                        cargarUsuarios();
                    } catch (IllegalArgumentException ex) {
                        mostrarError(ex.getMessage());
                    }
                    confirmacion.close();
                });
                btnConfirmar.addThemeVariants(ButtonVariant.LUMO_ERROR);

                Button btnCancelar = new Button("Cancelar", ev -> confirmacion.close());

                HorizontalLayout botones = new HorizontalLayout(btnConfirmar, btnCancelar);
                content.add(botones);
                confirmacion.add(content);
                confirmacion.open();
            });
            return btnEliminar;
        }).setHeader("Acciones");

        grid.setWidthFull();
    }

    private void abrirDialogoNuevoUsuario() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuevo usuario");

        TextField campoUsername = new TextField("Nombre de usuario");
        campoUsername.setWidthFull();

        PasswordField campoPassword = new PasswordField("Contraseña");
        campoPassword.setWidthFull();
        campoPassword.setHelperText("Mínimo 4 caracteres.");

        ComboBox<Rol> campoRol = new ComboBox<>("Rol");
        campoRol.setItems(Rol.values());
        campoRol.setValue(Rol.JUGADOR);
        campoRol.setWidthFull();

        Button btnCrear = new Button("Crear", e -> {
            try {
                usuarioService.registrarUsuario(
                        campoUsername.getValue(),
                        campoPassword.getValue(),
                        campoRol.getValue()
                );
                mostrarExito("Usuario \"" + campoUsername.getValue() + "\" creado correctamente.");
                cargarUsuarios();
                dialog.close();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        VerticalLayout content = new VerticalLayout(campoUsername, campoPassword, campoRol);
        content.setPadding(false);
        HorizontalLayout botones = new HorizontalLayout(btnCrear, btnCancelar);

        dialog.add(content, botones);
        dialog.open();
    }

    private void cargarUsuarios() {
        grid.setItems(usuarioService.getTodos());
    }

    private String obtenerUsernameActual() {
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
