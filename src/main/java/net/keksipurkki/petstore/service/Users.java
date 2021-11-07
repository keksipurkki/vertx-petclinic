package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.dto.LoginToken;
import net.keksipurkki.petstore.dto.User;

import java.util.Optional;

public interface Users {

    Future<User> create(User user);

    Future<Optional<User>> findByUsername(String username);

    Future<User> update(User old, User update);

    Future<Void> delete(User user);

    Future<LoginToken> login(User user, LoginToken token);

    Future<LoginToken> logout(LoginToken token);

    static Users create(Vertx vertx) {
        return new UsersImpl(vertx);
    }

}
