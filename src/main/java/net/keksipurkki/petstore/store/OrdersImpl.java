package net.keksipurkki.petstore.store;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrdersImpl implements Orders {
    private final Vertx vertx;
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();

    public OrdersImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<Map<Inventory, Integer>> getInventory() {
        return Future.succeededFuture(Inventory.counts());
    }

    @Override
    public Future<Order> place(NewOrder newOrder) {
        var order = Order.from(newOrder);
        orders.put(order.orderId(), order);
        return Future.succeededFuture(order);
    }

    @Override
    public Future<Optional<Order>> getById(int orderId) {
        var o = Optional.ofNullable(orders.get(orderId));
        return Future.succeededFuture(o);
    }

    @Override
    public Future<Optional<Order>> delete(int orderId) {
        var o = Optional.ofNullable(orders.remove(orderId));
        return Future.succeededFuture(o);
    }
}
