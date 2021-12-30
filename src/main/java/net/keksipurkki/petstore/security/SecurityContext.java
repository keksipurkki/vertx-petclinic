package net.keksipurkki.petstore.security;

import java.security.Principal;

public final class SecurityContext implements jakarta.ws.rs.core.SecurityContext {

    public static String REQUEST_CONTEXT_KEY = SecurityContext.class + "_CONTEXT";

    private final JwtPrincipal principal;

    public SecurityContext(JwtPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "BEARER";
    }
}
