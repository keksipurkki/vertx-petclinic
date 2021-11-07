package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserRecord;

import java.util.Optional;

public interface Users {

    Future<User> create(UserRecord data);

    Future<Optional<User>> findByUsername(String username);

    Future<User> update(String username, UserRecord update);

    Future<Optional<User>> delete(String username);

    Future<User> login(User user);

    Future<User> logout(User user);

    static Users create(Vertx vertx) {
        return new UsersImpl(vertx);
    }

}
