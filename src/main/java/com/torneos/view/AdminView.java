package com.torneos.view;

import com.torneos.model.Usuario;
import com.torneos.repository.UsuarioRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainView.class)
@PageTitle("Panel de Administración")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView(UsuarioRepository usuarioRepository) {
        H2 titulo = new H2("⚙️ Panel de Administración");

        H3 subtitulo = new H3("Usuarios del sistema");

        Grid<Usuario> grid = new Grid<>(Usuario.class, false);
        grid.addColumn(Usuario::getUsername).setHeader("Usuario");
        grid.addColumn(u -> u.getRol().name()).setHeader("Rol");
        grid.setItems(usuarioRepository.findAll());
        grid.setWidthFull();

        add(titulo, subtitulo, grid);
        setPadding(true);
        setSpacing(true);
    }
}
