package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserData;

import java.util.Optional;

public interface Users {

    Future<User> create(UserData data);

    Future<Optional<User>> findByUsername(String username);

    Future<User> update(String username, UserData update);

    Future<Void> delete(User user);

    static Users create(Vertx vertx) {
        return new UsersImpl(vertx);
    }

}
