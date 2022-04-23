package net.keksipurkki.petstore.pet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final public class PetStore {

    private static final Map<Status, Map<Integer, Pet>> store = Map.of(
        Status.AVAILABLE, new ConcurrentHashMap<>(),
        Status.PENDING, new ConcurrentHashMap<>(),
        Status.SOLD, new ConcurrentHashMap<>()
    );

    static public Map<Status, Integer> counts() {
        return Map.of(
            Status.AVAILABLE, store.get(Status.AVAILABLE).size(),
            Status.PENDING, store.get(Status.PENDING).size(),
            Status.SOLD, store.get(Status.SOLD).size()
        );
    }

    static public Collection<Pet> getInventory() {
        var result = new HashSet<Pet>();
        store.forEach((k, v) -> result.addAll(v.values()));
        return result;
    }

    public static void add(Pet pet) {
        store.get(Status.AVAILABLE).put(pet.id(), pet);
    }

    public static Pet delete(Pet pet) {
        return store.get(pet.status()).remove(pet.id());
    }

    public static Pet update(Pet pet) {
        var inventory = store.get(pet.status());
        inventory.put(pet.id(), pet);
        return pet;
    }

    public static void reserve(int petId) {
        var pet = store.get(Status.AVAILABLE).remove(petId);

        if (pet == null) {
            throw new UnsupportedOperationException("Pet is not available for reservation");
        }

        store.get(Status.PENDING).put(petId, pet);
    }

    public static void sell(int petId) {
        var pet = store.get(Status.PENDING).remove(petId);

        if (pet == null) {
            throw new UnsupportedOperationException("Pet is not available for selling");
        }

        store.get(Status.SOLD).put(petId, pet);
    }

}
