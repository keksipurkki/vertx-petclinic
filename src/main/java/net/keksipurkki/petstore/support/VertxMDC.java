package net.keksipurkki.petstore.support;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.nonNull;

public class VertxMDC {

    private final static String CONTEXT_KEY = VertxMDC.class + "_CONTEXT";

    private VertxMDC() {
    }

    public static void put(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            contextualDataMap(ctx).put(key, value);
        }
    }

    public static String get(String key) {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            return contextualDataMap(ctx).get(key);
        }
        return null;
    }

    public static String getOrDefault(String key, String defaultValue) {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            return contextualDataMap(ctx).getOrDefault(key, defaultValue);
        }
        return defaultValue;
    }

    public static Map<String, String> getAll() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            return new HashMap<>(contextualDataMap(ctx));
        }
        return Map.of();
    }

    public static ConcurrentMap<String, String> contextualDataMap(Context ctx) {
        var map = ctx.<ConcurrentMap<String, String>>getLocal(CONTEXT_KEY);
        if (nonNull(map)) {
            return map;
        } else {
            ctx.putLocal(CONTEXT_KEY, new ConcurrentHashMap<String, String>());
            return contextualDataMap(ctx);
        }
    }

}
