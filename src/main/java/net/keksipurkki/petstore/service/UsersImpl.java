package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserException;
import net.keksipurkki.petstore.model.UserRecord;

import java.util.Optional;

class UsersImpl implements Users {
    private final Vertx vertx;

    protected UsersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<User> create(UserRecord data) {
        return this.findByUsername(data.username()).flatMap(opt -> {
            if (opt.isPresent()) {
                throw new UserException("Username " + data.username() + " already exists");
            }
            return Future.succeededFuture(User.newUser(data));
        });
    }

    @Override
    public Future<Optional<User>> findByUsername(String username) {
        return Future.succeededFuture(User.existingUser(username));
    }

    @Override
    public Future<User> update(String username, UserRecord update) {
        return Future.succeededFuture(User.updatedUser(username, update));
    }

    @Override
    public Future<Optional<User>> delete(String username) {
        return Future.succeededFuture(User.deletedUser(username));
    }

    @Override
    public Future<User> login(User user) {
        return Future.succeededFuture(User.login(user));
    }

    @Override
    public Future<User> logout(User user) {
        return Future.succeededFuture(User.logout(user));
    }
}
