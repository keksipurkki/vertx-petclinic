package net.keksipurkki.petstore.support;

import io.vertx.core.json.JsonObject;

public interface JsonSerializable {
    JsonObject toJson();
}
