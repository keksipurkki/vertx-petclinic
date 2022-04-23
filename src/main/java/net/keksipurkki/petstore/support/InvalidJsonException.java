package net.keksipurkki.petstore.support;

import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import net.keksipurkki.petstore.api.ApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class InvalidJsonException extends ApiException {
    private final Set<ConstraintViolation<Object>> violations;

    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
        violations = Collections.emptySet();
    }

    public InvalidJsonException(String message, Set<ConstraintViolation<Object>> violations) {
        super(message, null);
        this.violations = violations;
    }

    @Override
    public int getStatusCode() {
        return 400;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }

    @JsonValue
    @Override
    public JsonObject toJson() {
        var json = super.toJson();
        if (!violations.isEmpty()) {
            var v = new ArrayList<JsonObject>();
            for (var violation : violations) {
                var reason = violation.getMessage();
                var property = violation.getPropertyPath();
                var invalid = violation.getInvalidValue();
                v.add(new JsonObject()
                    .put("reason", reason)
                    .put("property", property.toString())
                    .put("value", invalid.toString())
                );
            }
            json.put("violations", v);
        }
        return json;
    }

}
