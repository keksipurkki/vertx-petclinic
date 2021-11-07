package net.keksipurkki.petstore.api;

import io.vertx.core.Future;
import net.keksipurkki.petstore.dto.ApiMessage;
import net.keksipurkki.petstore.dto.LoginToken;
import net.keksipurkki.petstore.dto.User;
import net.keksipurkki.petstore.http.NotFoundException;
import net.keksipurkki.petstore.http.NotImplementedException;
import net.keksipurkki.petstore.service.Users;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Api implements ApiContract {

    private Users users;

    @Override
    public Future<ApiMessage> createUser(User user) {
        return users.create(user).map(v -> {
            var message = "User " + user.username() + " created successfully";
            return new ApiMessage(message);
        });
    }

    @Override
    public Future<ApiMessage> createWithList(List<User> userList) {
        return Future.failedFuture(new NotImplementedException());
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
    public Future<User> updateUser(String username, User user) {
        return getUserByName(username).flatMap(existing -> users.update(existing, user));
    }

    @Override
    public Future<ApiMessage> deleteUser(String username) {
        var deletion = getUserByName(username).flatMap(user -> users.delete(user));
        return deletion.map(v -> new ApiMessage("User " + username + "deleted successfully"));
    }

    @Override
    public Future<LoginToken> loginUser(String username, String password) {
        return getUserByName(username).flatMap(user -> {
            return users.login(user, LoginToken.from(username, password));
        });
    }

    @Override
    public Future<LoginToken> logout(LoginToken token) {
        return users.logout(token);
    }

    public Api withUsers(Users users) {
        this.users = requireNonNull(users, "Users service must be defined");
        return this;
    }

}
