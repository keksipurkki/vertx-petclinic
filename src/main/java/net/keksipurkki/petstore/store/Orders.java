package net.keksipurkki.petstore.store;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.keksipurkki.petstore.pet.Status;

import java.util.Map;
import java.util.Optional;

public interface Orders {

    Future<Map<Status, Integer>> getInventory();
    Future<Order> place(NewOrder newOrder);
    Future<Optional<Order>> getById(int orderId);
    Future<Optional<Order>> delete(int orderId);

    static Orders create(Vertx vertx) {
        return new OrdersImpl(vertx);
    }

}
