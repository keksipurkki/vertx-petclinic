package net.keksipurkki.petclinic.http;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import net.keksipurkki.petclinic.support.VertxMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

public final class Middlewares {

    public static String X_REQUEST_ID = "x-request-id";
    public static String X_SESSION_ID = "x-session-id";

    private final static Logger logger = LoggerFactory.getLogger(Middlewares.class);

    private Middlewares() {
    }

    public static BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    public static Handler<RoutingContext> routeNotFound() {
        return rc -> {
            rc.fail(new NotFoundException("Not Found"));
        };
    }

    public static Handler<RoutingContext> defaultHeaders() {
        return rc -> {
            rc.response().putHeader("cache-control", "no-cache");
            rc.next();
        };
    }

    public static Handler<RoutingContext> requestTracing(Map<String, String> environment) {

        final String APP_NAMESPACE = "APP_NAMESPACE"; // Microservice family
        final String APP_ENVIRONMENT = "APP_ENVIRONMENT"; // Runtime environment
        final String APP_NAME = "APP_NAME"; // Microservice name
        final String APP_TASK_ID = "APP_TASK_ID"; // Container task id

        final String FALLBACK = "N/A";

        return rc -> {

            var request = rc.request();
            var requestId = request.getHeader(X_REQUEST_ID);
            var sessionId = request.getHeader(X_SESSION_ID);

            var warn = requestId == null || sessionId == null;

            requestId = requireNonNullElse(requestId, UUID.randomUUID().toString());
            sessionId = requireNonNullElse(sessionId, UUID.randomUUID().toString());

            VertxMDC.put("requestId", requestId);
            VertxMDC.put("sessionId", sessionId);

            VertxMDC.put("app", environment.getOrDefault(APP_NAMESPACE, FALLBACK));
            VertxMDC.put("environment", environment.getOrDefault(APP_ENVIRONMENT, FALLBACK));
            VertxMDC.put("microservice", environment.getOrDefault(APP_NAME, FALLBACK));
            VertxMDC.put("taskId", environment.getOrDefault(APP_TASK_ID, FALLBACK));

            logger.debug("Request tracing enabled");

            if (warn) {
                logger.warn("Encountered empty values for correlation identifiers. The request will not be traceable across multiple service calls.");
            }

            rc.next();

        };
    }
}
