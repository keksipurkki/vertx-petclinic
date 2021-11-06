package net.keksipurkki.petclinic.api;

import io.vertx.core.json.JsonObject;

public class Api implements ApiContract {
    @Override
    public JsonObject greet() {
        return new JsonObject().put("message", "Hi!");
    }

    @Override
    public JsonObject meet() {
        return new JsonObject().put("message", "Yo!");
    }
}
