package net.keksipurkki.petstore.api;

import io.vertx.core.Future;
import net.keksipurkki.petstore.http.NotFoundException;
import net.keksipurkki.petstore.model.LoginToken;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserException;
import net.keksipurkki.petstore.model.UserRecord;
import net.keksipurkki.petstore.service.Users;
import net.keksipurkki.petstore.support.Futures;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Api implements ApiContract {

    private Users users;

    @Override
    public Future<ApiMessage> createUser(UserRecord data) {
        return users.create(data).map(v -> {
            var message = "User " + data.username() + " created successfully";
            return new ApiMessage(message);
        });
    }

    @Override
    public Future<ApiMessage> createWithList(List<UserRecord> userList) {

        if (!User.areUnique(userList)) {
            throw new UserException("Input contains duplicate users");
        }

        var future = userList
            .stream()
            .map(this::createUser)
            .collect(Futures.collector());

        return future
            .map(messages -> new ApiMessage("Created " + messages.size() + " users successfully"));

    }

    @Override
    public Future<User> getUserByName(String username) {
        return users.findByUsername(username).map(opt -> {
            if (opt.isEmpty()) {
                throw new NotFoundException("User " + username + " does not exist");
            }
            return opt.get();
        });
    }

    @Override
    public Future<User> updateUser(String username, UserRecord data) {
        return getUserByName(username)
            .flatMap(existing -> users.update(username, data));
    }

    @Override
    public Future<ApiMessage> deleteUser(String username) {
        return users.delete(username).map(user -> {

            if (user.isEmpty()) {
                throw new UserException("Attempted to delete a nonexistent user");
            }

            var message = "User " + user.get().record().username() + " deleted successfully";
            return new ApiMessage(message);

        });
    }

    @Override
    public Future<LoginToken> login(String username, String password) {
        return getUserByName(username).flatMap(user -> {

            if (!user.verifyPassword(password)) {
                throw new ForbiddenException("Invalid password");
            }

            return users.login(user).map(u -> LoginToken.from(u.record().username()));

        });
    }

    @Override
    public Future<ApiMessage> logout(LoginToken token) {
        var username = token.verifiedSubject();
        return getUserByName(username)
            .flatMap(user -> users.logout(user))
            .map(user -> new ApiMessage("User logged out"));
    }

    public Api withUsers(Users users) {
        this.users = requireNonNull(users, "Users service must be defined");
        return this;
    }

}
