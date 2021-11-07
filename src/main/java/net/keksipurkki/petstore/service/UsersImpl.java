package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserData;
import net.keksipurkki.petstore.model.UserException;

import java.util.Optional;

class UsersImpl implements Users {
    private final Vertx vertx;

    protected UsersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<User> create(UserData data) {
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
    public Future<User> update(String username, UserData update) {
        return Future.succeededFuture(User.updatedUser(username, update));
    }

    @Override
    public Future<Void> delete(User user) {
        return Future.succeededFuture(User.deletedUser(user.getData().username()))
                     .map(v -> null);
    }

}
