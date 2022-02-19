package net.keksipurkki.petstore.pet;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Optional;

class PetsImpl implements Pets {
    private final Vertx vertx;

    protected PetsImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<Pet> add(NewPet input) {
        var pet = Pet.from(input);
        PetStore.add(pet);
        return Future.succeededFuture(pet);
    }

    @Override
    public Future<Pet> update(Pet pet, PetImage image) {
        // Store image
        System.out.println(image.filename());
        System.out.println(image.contentType());
        System.out.println(image.metadata());

        // Update photoUrls
        pet.photoUrls().add(image.toUri());
        return Future.succeededFuture(pet);
    }

    @Override
    public Future<Pet> update(Pet pet) {
        return null;
    }

    @Override
    public Future<Optional<Pet>> getById(int petId) {
        return Future.succeededFuture(PetStore.getInventory().stream().filter(p -> p.id() == petId).findFirst());
    }

    @Override
    public Future<Optional<Pet>> delete(int petId) {
        return getById(petId).map(opt -> opt.map(PetStore::delete));
    }
}
