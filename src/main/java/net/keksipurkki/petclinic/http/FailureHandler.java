package net.keksipurkki.petclinic.http;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.keksipurkki.petclinic.api.ApiException;
import net.keksipurkki.petclinic.api.UnexpectedApiException;
import net.keksipurkki.petclinic.support.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;

public class FailureHandler implements Handler<RoutingContext> {

    private final static Logger logger = LoggerFactory.getLogger(FailureHandler.class);

    private final Map<Class<? extends Throwable>, FailureMapper<?>> map = new ConcurrentHashMap<>();
    private final FailureMapper<Throwable> FALLBACK_MAPPER = e -> new UnexpectedApiException("Unhandled exception", e);

    public FailureHandler() {
    }

    public <T extends Throwable> void on(Class<T> cls, FailureMapper<T> mapper) {
        map.put(cls, mapper);
    }

    @Override
    public void handle(RoutingContext routingContext) {

        if (!routingContext.failed()) {
            throw new IllegalStateException("Expected routing context failure");
        }

        try {

            var failure = routingContext.failure();
            var mapper = getFailureHandler(failure);

            logger.error("Request failed", failure);

            if (nonNull(failure.getCause())) {
                var cause = failure.getCause();
                logger.debug("Cause {}: {}", cause.getClass(), cause.getMessage());
            }

            errorResponse(routingContext, mapper.map(failure));

        } catch (Throwable cause) {

            logger.warn("Error in error handler! Producing an internal server error", cause);
            safeErrorResponse(routingContext, cause);

        }

    }

    @SuppressWarnings("unchecked")
    private FailureMapper<? super Throwable> getFailureHandler(Throwable throwable) {
        Class<?> key = throwable.getClass();
        List<Class<?>> classes = new ArrayList<>();

        if (map.containsKey(key)) {
            logger.trace("Found a configured failure handler for {}", key);
            return (FailureMapper<? super Throwable>) map.get(key);
        }

        logger.trace("Failure handler lookup from the super classes of {}", key);

        while (key != null) {
            classes.add(key);
            key = key.getSuperclass();
        }

        for (var cls : classes) {
            if (map.containsKey(cls)) {
                return (FailureMapper<? super Throwable>) map.get(cls);
            }
        }

        logger.warn("No failure handler for {} or any of its super classes. Fallback to default mapper", throwable.getClass());
        map.put(throwable.getClass(), FALLBACK_MAPPER);

        return FALLBACK_MAPPER;

    }

    protected void errorResponse(RoutingContext rc, ApiException failure) {

        failure.setInstance(URI.create(rc.request().absoluteURI()));

        rc.response()
          .setStatusCode(failure.getStatusCode())
          .putHeader("content-type", ApiException.MEDIA_TYPE)
          .end(Json.stringify(failure, true));
    }

    protected void safeErrorResponse(RoutingContext rc, Throwable failure) {
        var internalServerError = new UnexpectedApiException("Internal server error", failure);

        rc.response()
          .setStatusCode(500)
          .putHeader("content-type", ApiException.MEDIA_TYPE)
          .end(Json.stringify(internalServerError, true));
    }

    @FunctionalInterface
    public interface FailureMapper<T extends Throwable> {
        ApiException map(T throwable);
    }

}
