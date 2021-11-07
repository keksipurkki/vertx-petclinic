package net.keksipurkki.petstore.model;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class User {
    private static final AtomicInteger counter = new AtomicInteger();
    private static final Map<String, User> users = new ConcurrentHashMap<>();

    private final int id;
    private final UserData data;

    private User(int id, UserData record) {
        this.id = id;
        this.data = record;
    }

    public static boolean areUnique(List<UserData> userList) {
        var unique = userList.stream().map(UserData::username).collect(Collectors.toSet());
        return userList.size() == unique.size();
    }

    // :-/
    public static void clear() {
        users.clear();
    }

    public UserData getData() {
        return data;
    }

    public boolean verifyPassword(String password) {
        return MessageDigest.isEqual(password.getBytes(UTF_8), data.password().getBytes(UTF_8));
    }

    public static User newUser(UserData data) {
        var user = new User(counter.getAndIncrement(), data);
        users.put(user.data.username(), user);
        return user;
    }

    public static Optional<User> existingUser(String username) {
        var user = users.get(username);
        return Optional.ofNullable(user);
    }

    public static User updatedUser(String username, UserData update) {
        var existing = existingUser(username);

        if (existing.isEmpty()) {
            throw new UserException("No user exists with username " + username);
        }

        var user = existing.get();
        var updated = new User(user.id, update);
        users.put(username, updated);
        return updated;

    }

    public static Optional<User> deletedUser(String username) {
        return Optional.ofNullable(users.remove(username));
    }

    public enum Status {
        LOGGED_IN,
        LOGGED_OUT
    }

}
