package net.keksipurkki.petstore.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {
    }

    private static final ObjectMapper om;

    static {
        om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL); // JavaScript undefined semantics
        om.addMixIn(JsonObject.class, JsonObjectMixin.class)
            .addMixIn(JsonArray.class, JsonArrayMixin.class);
        // .. Apply object mapper configuration
    }

    @SneakyThrows
    public static <T> String stringify(T input, boolean prettyPrint) {
        if (prettyPrint) {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        } else {
            return om.writeValueAsString(input);
        }
    }

    public static <T> String stringify(T input) {
        return stringify(input, false);
    }

    // TODO: Apply bean validation
    public static <T> T parse(JsonObject json, Class<T> target) {
        return om.convertValue(json, target);
    }

    public static <T> List<T> parse(JsonArray array, Class<T[]> target) {
        var raw = om.convertValue(array, target);
        return List.of(raw);
    }

    private static abstract class JsonObjectMixin {
        @JsonCreator
        JsonObjectMixin(Map<String, Object> map) {
        }

        @JsonValue
        abstract Map<String, Object> getMap();
    }

    private static abstract class JsonArrayMixin {

        @JsonCreator
        JsonArrayMixin(List<?> list) {
        }

        @JsonValue
        abstract List<?> getList();
    }

}
