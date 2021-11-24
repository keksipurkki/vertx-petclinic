package net.keksipurkki.petstore.store;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.Optional;

public class OrdersImpl implements Orders {
    private final Vertx vertx;

    public OrdersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<Map<Inventory, Integer>> getInventory() {
        return Future.succeededFuture(Inventory.counts());
    }

    @Override
    public Future<Order> place(NewOrder newOrder) {
        return Future.succeededFuture(Order.from(newOrder));
    }

    @Override
    public Future<Optional<Order>> getById(int orderId) {
        return Future.succeededFuture(Optional.empty());
    }
}
