package net.keksipurkki.petstore.api;

import io.vertx.core.Future;
import net.keksipurkki.petstore.http.ForbiddenException;
import net.keksipurkki.petstore.http.NotFoundException;
import net.keksipurkki.petstore.http.NotImplementedException;
import net.keksipurkki.petstore.security.JwtPrincipal;
import net.keksipurkki.petstore.security.SecurityContext;
import net.keksipurkki.petstore.store.Inventory;
import net.keksipurkki.petstore.store.NewOrder;
import net.keksipurkki.petstore.store.Order;
import net.keksipurkki.petstore.store.Orders;
import net.keksipurkki.petstore.support.Futures;
import net.keksipurkki.petstore.user.AccessToken;
import net.keksipurkki.petstore.user.User;
import net.keksipurkki.petstore.user.UserException;
import net.keksipurkki.petstore.user.Users;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Api implements ApiContract {

    private Users users;
    private SecurityContext context;
    private Orders orders;

    // User

    @Override
    public Future<ApiMessage> createUser(User data) {
        return users.create(data).map(v -> {
            var message = "User " + data.username() + " created successfully";
            return new ApiMessage(message);
        });
    }

    @Override
    public Future<ApiMessage> createWithList(List<User> userList) {

        if (!users.areUnique(userList)) {
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
        return users.findByUsername(username)
                    .map(opt -> opt.orElseThrow(() -> new NotFoundException("User " + username + " does not exist")))
                    .map(User::redactCredentials);
    }

    @Override
    public Future<User> updateUser(String username, User data) {
        return getUserByName(username)
            .flatMap(existing -> users.update(username, data))
            .map(User::redactCredentials);
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
        return this.users.findByUsername(username)
                         .map(opt -> opt.orElseThrow(() -> new NotFoundException("User " + username + " does not exist")))
                         .map(user -> {

                             if (!user.verifyPassword(password)) {
                                 throw new ForbiddenException("Invalid password");
                             }

                             return JwtPrincipal.from(user.username());

                         }).map(principal -> new AccessToken(principal.getToken()));
    }

    @Override
    public Future<ApiMessage> logout() {
        throw new NotImplementedException();
    }

    // Store

    @Override
    public Future<Map<Inventory, Integer>> getInventory() {
        return orders.getInventory();
    }

    @Override
    public Future<Order> placeOrder(NewOrder newOrder) {
        return orders.place(newOrder);
    }

    @Override
    public Future<Order> getOrderById(int orderId) {
        return orders.getById(orderId)
                     .map(opt -> opt.orElseThrow(() -> new NotFoundException("Order " + orderId + " does not exist")));
    }

    @Override
    public Future<Order> deleteOrder(int orderId) {
        return orders.delete(orderId)
                     .map(opt -> opt.orElseThrow(() -> new NotFoundException("Order " + orderId + " does not exist")));
    }

    public Api withUsers(Users users) {
        var clone = new Api();
        clone.users = requireNonNull(users, "Users service must be defined");
        clone.orders = orders;
        clone.context = context;
        return clone;
    }

    public Api withOrders(Orders orders) {
        var clone = new Api();
        clone.orders = requireNonNull(orders, "Orders service must be defined");
        clone.users = users;
        clone.context = context;
        return clone;
    }

    public Api withSecurityContext(SecurityContext context) {
        var clone = new Api();
        clone.users = users;
        clone.orders = orders;
        clone.context = context;
        return clone;
    }

}
