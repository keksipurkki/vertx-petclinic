package net.keksipurkki.petstore.store;

import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;

public record Order(int orderId, int petId, int quantity, ZonedDateTime shipDate, String status) {

    public static Order from(NewOrder order) {
        var orderId = ThreadLocalRandom.current().nextInt() & Integer.MAX_VALUE;
        return new Order(orderId, order.petId(), order.quantity(), ZonedDateTime.now(), "placed");
    }

}
