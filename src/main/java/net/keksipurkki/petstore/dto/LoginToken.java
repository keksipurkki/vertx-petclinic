package net.keksipurkki.petstore.dto;

import net.keksipurkki.petstore.api.UnexpectedApiException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record LoginToken(String token) {

    private static byte[] SECRET = "hushhush".getBytes(StandardCharsets.UTF_8);

    public static LoginToken from(String username, String password) {
        try {

            var credentials = String.format("%s:%s", username, password);
            var mac = Mac.getInstance("HmacSHA256");
            var secretKeySpec = new SecretKeySpec(SECRET, "HmacSHA256");
            mac.init(secretKeySpec);
            var hmac = mac.doFinal(credentials.getBytes(StandardCharsets.UTF_8));
            var token = Base64.getEncoder().encodeToString(hmac);
            return new LoginToken(token);

        } catch (Exception cause) {
            throw new UnexpectedApiException("Login token computation failed", cause);
        }
    }
}
