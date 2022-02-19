package net.keksipurkki.petstore.pet;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record Pet(int id, Category category, String name, List<URI> photoUrls, List<String> tags, Status status) {

    public static Pet from(NewPet input) {
        var petId = ThreadLocalRandom.current().nextInt() & Integer.MAX_VALUE;
        var category = Category.from(input.category());
        return new Pet(petId, category, input.name(), new ArrayList<>(), new ArrayList<>(), Status.AVAILABLE);
    }

}
