package com.torneos.view;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.torneos.dto.TorneoResponse;
import com.torneos.model.Rol;
import com.torneos.model.Usuario;
import com.torneos.service.TorneoService;
import com.torneos.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private final TorneoService torneoService;
    private final Grid<Usuario> grid = new Grid<>(Usuario.class, false);
    private final Grid<TorneoResponse> gridTorneos = new Grid<>(TorneoResponse.class, false);

    public AdminView(UsuarioService usuarioService, TorneoService torneoService) {
        this.usuarioService = usuarioService;
        this.torneoService = torneoService;

        H2 titulo = new H2("⚙️ Panel de Administración");

        // ── KPI Cards ─────────────────────────────────────────────────────────
        List<Usuario> todos = usuarioService.getTodos();
        long admins = todos.stream().filter(u -> u.getRol() == Rol.ADMIN).count();
        long organizadores = todos.stream().filter(u -> u.getRol() == Rol.ORGANIZADOR).count();
        long jugadores = todos.stream().filter(u -> u.getRol() == Rol.JUGADOR).count();

        HorizontalLayout kpis = new HorizontalLayout(
                crearKpi("👑 Admins", String.valueOf(admins), "#fdecea", "#b71c1c"),
                crearKpi("📋 Organizadores", String.valueOf(organizadores), "#e8eaf6", "#1a237e"),
                crearKpi("⚽ Jugadores", String.valueOf(jugadores), "#e6f4ea", "#1e7e34"),
                crearKpi("👥 Total usuarios", String.valueOf(todos.size()), "#fff8e1", "#e65100")
        );
        kpis.setWidthFull();
        kpis.setSpacing(true);

        H3 subtitulo = new H3("Gestión de usuarios");

        configurarGrid();
        cargarUsuarios();

        Button btnNuevoUsuario = new Button("➕ Nuevo usuario", new Icon(VaadinIcon.PLUS),
                e -> abrirDialogoNuevoUsuario());
        btnNuevoUsuario.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // ── Sección Gestión de Torneos ────────────────────────────────────────
        H3 tituloTorneos = new H3("Gestión de torneos");
        configurarGridTorneos();
        cargarTorneos();

        add(titulo, kpis, subtitulo, btnNuevoUsuario, grid, tituloTorneos, gridTorneos);
        setPadding(true);
        setSpacing(true);
    }

    private VerticalLayout crearKpi(String etiqueta, String valor, String bgColor, String textColor) {
        Span valorSpan = new Span(valor);
        valorSpan.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", textColor);

        Span etiquetaSpan = new Span(etiqueta);
        etiquetaSpan.getStyle()
                .set("font-size", "0.85rem")
                .set("color", textColor)
                .set("opacity", "0.8");

        VerticalLayout card = new VerticalLayout(valorSpan, etiquetaSpan);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", bgColor)
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("flex", "1")
                .set("min-width", "130px");
        return card;
    }

    private void configurarGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Usuario::getUsername).setHeader("Usuario").setSortable(true);

        grid.addComponentColumn(u -> {
            Span badge = new Span(u.getRol().name());
            String theme = u.getRol() == Rol.ADMIN ? "error" :
                           u.getRol() == Rol.ORGANIZADOR ? "contrast" : "success";
            badge.getElement().getThemeList().add("badge " + theme);
            return badge;
        }).setHeader("Rol").setSortable(false);

        grid.addComponentColumn(usuario -> {
            String usernameActual = obtenerUsernameActual();
            ComboBox<Rol> comboRol = new ComboBox<>();
            comboRol.setItems(Rol.values());
            comboRol.setValue(usuario.getRol());
            comboRol.setWidth("150px");
            boolean esMismo = usuario.getUsername().equals(usernameActual);
            comboRol.setEnabled(!esMismo);
            if (esMismo) comboRol.setHelperText("No puedes cambiar tu propio rol.");
            comboRol.addValueChangeListener(event -> {
                if (event.getValue() != null && event.getValue() != event.getOldValue()) {
                    try {
                        usuarioService.cambiarRol(usuario.getId(), usernameActual, event.getValue());
                        mostrarExito("Rol de " + usuario.getUsername() + " actualizado.");
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
            Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
            btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
            btnEliminar.setEnabled(!usuario.getUsername().equals(usernameActual));
            btnEliminar.addClickListener(e -> {
                Dialog confirmacion = new Dialog();
                confirmacion.setHeaderTitle("Confirmar eliminación");
                VerticalLayout content = new VerticalLayout();
                content.add("¿Eliminar al usuario \"" + usuario.getUsername() + "\"?");
                Button btnConfirmar = new Button("Eliminar", ev -> {
                    try {
                        usuarioService.eliminarUsuario(usuario.getId(), usernameActual);
                        mostrarExito("Usuario eliminado correctamente.");
                        cargarUsuarios();
                    } catch (IllegalArgumentException ex) {
                        mostrarError(ex.getMessage());
                    } catch (Exception ex) {
                        mostrarError("No se puede eliminar: tiene datos asociados.");
                    }
                    confirmacion.close();
                });
                btnConfirmar.addThemeVariants(ButtonVariant.LUMO_ERROR);
                Button btnCancelar = new Button("Cancelar", ev -> confirmacion.close());
                content.add(new HorizontalLayout(btnConfirmar, btnCancelar));
                confirmacion.add(content);
                confirmacion.open();
            });
            return btnEliminar;
        }).setHeader("Acciones").setWidth("100px").setFlexGrow(0);

        grid.setWidthFull();
    }

    private void abrirDialogoNuevoUsuario() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("➕ Nuevo usuario");

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
                usuarioService.registrarUsuario(campoUsername.getValue(),
                        campoPassword.getValue(), campoRol.getValue());
                mostrarExito("Usuario \"" + campoUsername.getValue() + "\" creado.");
                cargarUsuarios();
                dialog.close();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage());
            }
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());
        dialog.add(new VerticalLayout(campoUsername, campoPassword, campoRol),
                new HorizontalLayout(btnCrear, btnCancelar));
        dialog.open();
    }

    private void cargarUsuarios() { grid.setItems(usuarioService.getTodos()); }
    private void cargarTorneos() { gridTorneos.setItems(torneoService.getTorneos()); }

    private void configurarGridTorneos() {
        gridTorneos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridTorneos.addColumn(TorneoResponse::nombre).setHeader("Torneo").setSortable(true);
        gridTorneos.addColumn(TorneoResponse::deporte).setHeader("Deporte");
        gridTorneos.addColumn(TorneoResponse::categoria).setHeader("Categoría");
        gridTorneos.addColumn(t -> t.formato().getLabel()).setHeader("Formato");
        gridTorneos.addComponentColumn(t -> {
            Span badge = new Span(t.activo() ? "Activo" : "Inactivo");
            badge.getElement().getThemeList().add(t.activo() ? "badge success" : "badge error");
            return badge;
        }).setHeader("Estado").setWidth("100px").setFlexGrow(0);

        gridTorneos.addComponentColumn(t -> {
            Button btnFixture = new Button("🗑️ Fixture", new Icon(VaadinIcon.TRASH));
            btnFixture.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY);
            btnFixture.addClickListener(e -> confirmarAccion(
                    "¿Eliminar todos los partidos del torneo \"" + t.nombre() + "\"?",
                    "Esto borrará el fixture completo pero mantiene el torneo y los equipos.",
                    () -> {
                        torneoService.eliminarFixture(t.id());
                        mostrarExito("Fixture del torneo \"" + t.nombre() + "\" eliminado.");
                        cargarTorneos();
                    }));

            Button btnTorneo = new Button("🗑️ Torneo", new Icon(VaadinIcon.TRASH));
            btnTorneo.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnTorneo.addClickListener(e -> confirmarAccion(
                    "¿Eliminar el torneo \"" + t.nombre() + "\" completamente?",
                    "Se eliminarán el torneo, todos sus equipos, jugadores, partidos y estadísticas. Esta acción NO se puede deshacer.",
                    () -> {
                        torneoService.eliminarTorneo(t.id());
                        mostrarExito("Torneo \"" + t.nombre() + "\" eliminado.");
                        cargarTorneos();
                    }));

            HorizontalLayout acciones = new HorizontalLayout(btnFixture, btnTorneo);
            acciones.setSpacing(true);
            return acciones;
        }).setHeader("Acciones").setWidth("220px").setFlexGrow(0);

        gridTorneos.setWidthFull();
    }

    private void confirmarAccion(String pregunta, String detalle, Runnable accion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚠️ Confirmar acción");

        Span msg = new Span(pregunta);
        msg.getStyle().set("font-weight", "600");

        Span det = new Span(detalle);
        det.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Button btnConfirmar = new Button("Confirmar", e -> {
            try {
                accion.run();
            } catch (Exception ex) {
                mostrarError("Error: " + ex.getMessage());
            }
            dialog.close();
        });
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        VerticalLayout content = new VerticalLayout(msg, det,
                new HorizontalLayout(btnConfirmar, btnCancelar));
        content.setSpacing(true);
        dialog.add(content);
        dialog.open();
    }
    private String obtenerUsernameActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }
    private void mostrarExito(String m) {
        Notification n = Notification.show(m, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    private void mostrarError(String m) {
        Notification n = Notification.show(m, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
