package net.keksipurkki.petstore.store;

import io.vertx.core.impl.ConcurrentHashSet;
import net.keksipurkki.petstore.pet.Pet;

import java.util.Map;
import java.util.Set;

public enum Inventory {
    AVAILABLE,
    PENDING,
    SOLD;

    private final Set<Pet> inventory;

    Inventory() {
        this.inventory = new ConcurrentHashSet<>();
    }

    static public Map<Inventory, Integer> counts() {
        return Map.of(
            AVAILABLE, AVAILABLE.inventory.size(),
            PENDING, PENDING.inventory.size(),
            SOLD, SOLD.inventory.size()
        );
    }

}
