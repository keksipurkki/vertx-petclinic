package net.keksipurkki.petstore.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.dto.LoginToken;
import net.keksipurkki.petstore.dto.User;
import net.keksipurkki.petstore.support.Json;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class UsersImpl implements Users {
    private final Vertx vertx;

    private final Map<User, LoginToken> logins = new ConcurrentHashMap<>();

    protected UsersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<User> create(User user) {
        return this.findByUsername(user.username()).flatMap(opt -> {
            if (opt.isPresent()) {
                throw new UserException("Username " + user.username() + " already exists");
            }
            return Future.succeededFuture(user);
        });
    }

    @Override
    public Future<Optional<User>> findByUsername(String username) {
        return Future.succeededFuture(Optional.empty());
    }

    @Override
    public Future<User> update(User old, User update) {
        var a = JsonObject.mapFrom(old);
        var b = JsonObject.mapFrom(update);
        var user = Json.parse(a.mergeIn(b), User.class);
        return Future.succeededFuture(user);
    }

    @Override
    public Future<Void> delete(User user) {
        return Future.succeededFuture(null);
    }

    @Override
    public Future<LoginToken> login(User user, LoginToken token) {
        logins.put(user, token);
        return Future.succeededFuture(token);
    }

    @Override
    public Future<LoginToken> logout(LoginToken token) {

        var entry = logins.entrySet()
            .stream()
            .filter(e -> e.getValue().equals(token))
            .findFirst();

        if (entry.isEmpty()) {
            throw new UserException("Use must be logged in before logging out");
        }

        var user = entry.get().getKey();

        return Future.succeededFuture(logins.remove(user));

    }
}
