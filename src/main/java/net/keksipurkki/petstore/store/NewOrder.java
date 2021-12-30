package net.keksipurkki.petstore.store;

import jakarta.validation.constraints.Min;

public record NewOrder(int petId, @Min(1) int quantity) {
}
