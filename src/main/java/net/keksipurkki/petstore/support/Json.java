package net.keksipurkki.petstore.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {
    }

    private static ObjectMapper om;

    static {
        om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL); // JavaScript undefined semantics
        om.addMixIn(JsonObject.class, JsonObjectMixin.class)
          .addMixIn(JsonArray.class, JsonArrayMixin.class);
        // .. Apply object mapper configuration
    }

    public static <T> String stringify(T input, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return om.writerWithDefaultPrettyPrinter().writeValueAsString(input);
            } else {
                return om.writeValueAsString(input);
            }
        } catch (JsonProcessingException cause) {
            throw new InvalidJsonException("JSON serialization failed", cause);
        }
    }

    public static <T> String stringify(T input) {
        return stringify(input, false);
    }

    // TODO: Apply bean validation
    public static <T> T parse(JsonObject json, Class<T> target) {
        return om.convertValue(json, target);
    }

    public static <T> T parse(String jsonString, Class<T> target) {
        var json = new JsonObject(jsonString);
        return parse(json, target);
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
