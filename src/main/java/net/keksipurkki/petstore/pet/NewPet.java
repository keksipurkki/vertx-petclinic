package net.keksipurkki.petstore.pet;

import jakarta.validation.constraints.NotNull;

public record NewPet(@NotNull String name, @NotNull String category) {
}
