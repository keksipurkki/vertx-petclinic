package net.keksipurkki.petstore.pet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public record Category(int id, String name) {

    private static final Map<String, Category> categories = new ConcurrentHashMap<>();

    public static Category from(String name) {
        return categories.computeIfAbsent(name, k -> {
            var id = ThreadLocalRandom.current().nextInt() & Integer.MAX_VALUE;
            return new Category(id, k);
        });
    }
}
