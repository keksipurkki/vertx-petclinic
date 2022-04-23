package net.keksipurkki.petstore.user;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class UsersImpl implements Users {

    private static final Map<String, User> users = new ConcurrentHashMap<>();
    private final Vertx vertx;

    public UsersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    // :-/
    public static void clear() {
        users.clear();
    }

    private static User newUser(User data) {
        users.put(data.username(), data);
        return data;
    }

    private static Optional<User> existingUser(String username) {
        var user = users.get(username);
        return Optional.ofNullable(user);
    }

    private static User updatedUser(String username, User update) {
        var existing = existingUser(username);

        if (existing.isEmpty()) {
            throw new UserException("No user exists with username " + username);
        }

        users.put(username, update);
        return update;

    }

    private static Optional<User> deletedUser(String username) {
        return Optional.ofNullable(users.remove(username));
    }

    /* Async Vert.x powered API */

    @Override
    public Future<User> create(User data) {
        return this.findByUsername(data.username()).flatMap(opt -> {
            if (opt.isPresent()) {
                throw new UserException("Username " + data.username() + " already exists");
            }
            return Future.succeededFuture(newUser(data));
        });
    }

    @Override
    public Future<Optional<User>> findByUsername(String username) {
        return Future.succeededFuture(existingUser(username));
    }

    @Override
    public Future<User> update(String username, User update) {
        return Future.succeededFuture(updatedUser(username, update));
    }

    @Override
    public Future<Void> delete(User user) {
        return Future.succeededFuture(deletedUser(user.username()))
                     .map(v -> null);
    }

    @Override
    public boolean areUnique(List<User> userList) {
        var unique = userList.stream().map(User::username).collect(Collectors.toSet());
        return userList.size() == unique.size();
    }

}
