package net.keksipurkki.petstore.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import net.keksipurkki.petstore.api.UnexpectedApiException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Json {
    private Json() {
    }

    private static final ObjectMapper om;
    private static final Validator validator;

    static {

        om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        om.setSerializationInclusion(JsonInclude.Include.NON_NULL); // JavaScript undefined semantics

        om.addMixIn(JsonObject.class, JsonObjectMixin.class);
        om.addMixIn(JsonArray.class, JsonArrayMixin.class);

        // Apply object mapper configuration...
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static <T> String stringify(T input, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return om.writerWithDefaultPrettyPrinter().writeValueAsString(input);
            } else {
                return om.writeValueAsString(input);
            }
        } catch (JsonProcessingException cause) {
            throw new UnexpectedApiException("JSON serialization failed", cause);
        }
    }

    public static <T> String stringify(T input) {
        return stringify(input, false);
    }

    public static <T> T parse(JsonObject json, Class<T> target) {
        var result = om.convertValue(json, target);
        return validate(result, target);
    }

    private static <T> T validate(T input, Class<T> target) {
        Set<ConstraintViolation<Object>> violations = validator.validate(input);
        if (violations.isEmpty()) {
            return input;
        } else {
            var msg = String.format("JSON is not a valid representation of %s", target.getSimpleName());
            throw new InvalidJsonException(msg, violations);
        }
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
