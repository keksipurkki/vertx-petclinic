package net.keksipurkki.petstore.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "User data")
public record UserData(
    @NotNull String username,
    @NotNull String firstName,
    @NotNull String lastName,
    @NotNull String email,
    @NotNull String password,

    String phone
) {
}