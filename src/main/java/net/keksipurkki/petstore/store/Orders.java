package net.keksipurkki.petstore.store;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.Optional;

public interface Orders {

    Future<Map<Inventory, Integer>> getInventory();
    Future<Order> place(NewOrder newOrder);
    Future<Optional<Order>> getById(int orderId);

    static Orders create(Vertx vertx) {
        return new OrdersImpl(vertx);
    }
}
