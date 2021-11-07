package net.keksipurkki.petstore.api;

import io.vertx.core.Future;
import net.keksipurkki.petstore.http.NotFoundException;
import net.keksipurkki.petstore.http.NotImplementedException;
import net.keksipurkki.petstore.model.AccessToken;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserData;
import net.keksipurkki.petstore.model.UserException;
import net.keksipurkki.petstore.security.JwtPrincipal;
import net.keksipurkki.petstore.security.SecurityContext;
import net.keksipurkki.petstore.service.Users;
import net.keksipurkki.petstore.support.Futures;

import javax.ws.rs.ForbiddenException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Api implements ApiContract {

    private Users users;
    private SecurityContext context;

    @Override
    public Future<ApiMessage> createUser(UserData data) {
        return users.create(data).map(v -> {
            var message = "User " + data.username() + " created successfully";
            return new ApiMessage(message);
        });
    }

    @Override
    public Future<ApiMessage> createWithList(List<UserData> userList) {

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
    public Future<User> updateUser(String username, UserData data) {
        return getUserByName(username)
            .flatMap(existing -> users.update(username, data));
    }

    @Override
    public Future<ApiMessage> deleteUser(String username) {
        return getUserByName(username)
            .flatMap(user -> users.delete(user))
            .map(user -> {
                var message = "User " + username + " deleted successfully";
                return new ApiMessage(message);
            });
    }

    @Override
    public Future<AccessToken> login(String username, String password) {
        return getUserByName(username).map(user -> {

            if (!user.verifyPassword(password)) {
                throw new ForbiddenException("Invalid password");
            }

            return JwtPrincipal.from(user.getData().username());

        }).map(principal -> new AccessToken(principal.getToken()));
    }

    @Override
    public Future<ApiMessage> logout() {
        throw new NotImplementedException();
    }

    public Api withUsers(Users users) {
        var clone = new Api();
        clone.users = requireNonNull(users, "Users service must be defined");
        clone.context = context;
        return clone;
    }

    public Api withSecurityContext(SecurityContext context) {
        var clone = new Api();
        clone.users = users;
        clone.context = context;
        return clone;
    }

}
