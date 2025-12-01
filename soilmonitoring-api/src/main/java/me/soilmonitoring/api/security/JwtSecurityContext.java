package me.soilmonitoring.api.security;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Set;

public class JwtSecurityContext implements SecurityContext {

    private final String username;
    private final Set<String> roles;
    private final boolean secure;

    public JwtSecurityContext(String username, Set<String> roles, boolean secure) {
        this.username = username;
        this.roles = roles;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
