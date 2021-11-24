package net.keksipurkki.petstore.store;

import java.time.ZonedDateTime;

public record Order(int orderId, int petId, int quantity, ZonedDateTime shipDate, String status) {

    public static Order from(NewOrder order) {
        return new Order(0, order.petId(), order.quantity(), ZonedDateTime.now(), "placed");
    }

}
