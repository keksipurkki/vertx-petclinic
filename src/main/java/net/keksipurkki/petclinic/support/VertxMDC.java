package net.keksipurkki.petclinic.support;

import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import net.keksipurkki.petclinic.http.Middlewares;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VertxMDC {

    private final static Logger logger = LoggerFactory.getLogger(Middlewares.class);

    private VertxMDC() {
    }

    public static void put(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        ContextInternal ctx = (ContextInternal) Vertx.currentContext();
        if (ctx == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Attempt to set contextual data from a non Vert.x thread", new Exception());
            }
        } else {
            contextualDataMap(ctx).put(key, value);
        }
    }

    public static String get(String key) {
        Objects.requireNonNull(key);
        ContextInternal ctx = (ContextInternal) Vertx.currentContext();
        if (ctx != null) {
            return contextualDataMap(ctx).get(key);
        }
        return null;
    }

    public static String getOrDefault(String key, String defaultValue) {
        Objects.requireNonNull(key);
        ContextInternal ctx = (ContextInternal) Vertx.currentContext();
        if (ctx != null) {
            return contextualDataMap(ctx).getOrDefault(key, defaultValue);
        }
        return defaultValue;
    }

    public static Map<String, String> getAll() {
        ContextInternal ctx = (ContextInternal) Vertx.currentContext();
        if (ctx != null) {
            return new HashMap<>(contextualDataMap(ctx));
        }
        return Map.of();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConcurrentMap<String, String> contextualDataMap(ContextInternal ctx) {
        ConcurrentMap<Object, Object> lcd = Objects.requireNonNull(ctx).localContextData();
        return (ConcurrentMap) lcd.computeIfAbsent(VertxMDC.class, k -> new ConcurrentHashMap());
    }

}
