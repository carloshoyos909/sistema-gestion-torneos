package com.torneos.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    // Retorna el nombre de usuario (email o username) del token JWT actual
    public String getAuthenticatedUsername() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElse(null);
    }

    // Invalida la sesión actual y redirige al login
    public void logout() {
        authenticationContext.logout();
    }
}