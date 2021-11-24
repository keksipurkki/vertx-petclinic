package net.keksipurkki.petstore.api;

import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.support.JsonSerialization;

import java.net.URI;

/**
 * An exception that serializes to the application/problem+json media type
 * <p>
 * All non-2xx responses of the API must conform to this media type
 *
 * @see "https://datatracker.ietf.org/doc/html/rfc7807"
 */
public abstract class ApiException extends RuntimeException implements JsonSerialization {

    public static final String MEDIA_TYPE = "application/problem+json";
    public static final URI DEFAULT_TYPE = URI.create("about:blank");

    private URI instance;

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(String message) {
        super(message, null);
    }

    public abstract int getStatusCode();

    public String getTitle() {
        return this.getClass().getSimpleName();
    }

    public abstract String getDetail();

    public URI getType() {
        return DEFAULT_TYPE;
    }

    public void setInstance(URI uri) {
        this.instance = uri;
    }

    public URI getInstance() {
        return this.instance;
    }

    @JsonValue
    @Override
    public JsonObject toJson() {
        return new JsonObject()
            .put("type", getType())
            .put("title", getTitle())
            .put("status", getStatusCode())
            .put("detail", getDetail())
            .put("instance", getInstance());
    }

}
