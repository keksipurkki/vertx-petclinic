package net.keksipurkki.petstore.user;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.security.MessageDigest;

import static java.nio.charset.StandardCharsets.UTF_8;

@Schema(description = "User data")
public record User(

    @NotNull Integer id,
    @NotNull String username,
    @NotNull String firstName,
    @NotNull String lastName,
    @NotNull String email,
    @NotNull String password,
    String phone
) {

    public boolean verifyPassword(String password) {
        return MessageDigest.isEqual(password.getBytes(UTF_8), this.password.getBytes(UTF_8));
    }

    public User redactCredentials() {
        return new User(id, username, firstName, lastName, email, null, phone);
    }

    public User redactPersonalData() {
        return new User(id, null, null, null, null, null, null);
    }

}