package net.keksipurkki.petclinic.service;

import io.vertx.core.Vertx;

public interface Pets {

    static Pets create(Vertx vertx) {
        return new PetsImpl(vertx);
    }

}
