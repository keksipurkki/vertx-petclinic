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
    private final UserRecord record;
    private final Status status;

    private User(int id, UserRecord record) {
        this.id = id;
        this.record = record;
        this.status = Status.LOGGED_OUT;
    }

    private User(User user, Status status) {
        this.id = user.id;
        this.record = user.record;
        this.status = status;
    }

    public static boolean areUnique(List<UserRecord> userList) {
        var unique = userList.stream().map(UserRecord::username).collect(Collectors.toSet());
        return userList.size() == unique.size();
    }

    public UserRecord record() {
        return record;
    }

    public boolean verifyPassword(String password) {
        return MessageDigest.isEqual(password.getBytes(UTF_8), record.password().getBytes(UTF_8));
    }

    public static User newUser(UserRecord data) {
        var user = new User(counter.getAndIncrement(), data);
        users.put(user.record.username(), user);
        return user;
    }

    public static Optional<User> existingUser(String username) {
        var user = users.get(username);
        return Optional.ofNullable(user);
    }

    public static User updatedUser(String username, UserRecord update) {
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

    public static User login(User user) {
        var u = new User(user, Status.LOGGED_IN);
        users.put(user.record.username(), u);
        return u;
    }

    public static User logout(User user) {
        var u = new User(user, Status.LOGGED_OUT);
        users.put(user.record.username(), u);
        return u;
    }

    public enum Status {
        LOGGED_IN,
        LOGGED_OUT
    }

}
