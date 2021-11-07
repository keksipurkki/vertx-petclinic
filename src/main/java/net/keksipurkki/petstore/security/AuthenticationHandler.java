package net.keksipurkki.petstore.security;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.keksipurkki.petstore.api.ApiOperation;

import static java.util.Objects.isNull;

public final class AuthenticationHandler implements Handler<RoutingContext> {

    private AuthenticationHandler() {
    }

    @Override
    public void handle(RoutingContext rc) {

        final var prefix = "Bearer ";
        var authorization = rc.request().getHeader("Authorization");

        if (isNull(authorization)) {
            throw new SecurityException("Missing Authorization header");
        }

        if (!authorization.startsWith(prefix)) {
            throw new SecurityException("Invalid Authorization header value");
        }

        var jwt = authorization.substring(prefix.length()).trim();

        try {

            var principal = new JwtPrincipal(jwt);
            var securityContext = new SecurityContext(principal);

            rc.put(SecurityContext.REQUEST_CONTEXT_KEY, securityContext);
            rc.next();

        } catch (SecurityException exception) {
            rc.fail(exception);
        }

    }

    public static Handler<RoutingContext> create(ApiOperation operation) {

        if (operation.getSecurityScheme().equals(SecurityScheme.NONE)) {
            return RoutingContext::next;
        } else {
            return new AuthenticationHandler();
        }
    }

}
