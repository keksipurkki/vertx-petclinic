package net.keksipurkki.petstore.model;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.keksipurkki.petstore.api.UnexpectedApiException;
import net.keksipurkki.petstore.http.ForbiddenException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public record LoginToken(String token) {

    private static final byte[] SECRET = "hushhush".getBytes(StandardCharsets.UTF_8);
    private static final long SESSION_TTL = TimeUnit.MINUTES.toMillis(15);

    public static LoginToken from(String username) {
        try {

            var signer = new MACSigner(SECRET);
            var header = new JWSHeader(JWSAlgorithm.HS256);

            var claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer("https://keksipurkki.net")
                .expirationTime(new Date(new Date().getTime() + SESSION_TTL))
                .build();

            var jwt = new SignedJWT(header, claimsSet);
            jwt.sign(signer);

            return new LoginToken(jwt.serialize());

        } catch (Exception cause) {
            throw new UnexpectedApiException("Login token computation failed", cause);
        }
    }

    public String verifiedSubject() throws TokenException {
        try {
            var signed = SignedJWT.parse(token);
            var verifier = new MACVerifier(SECRET);

            if (!signed.verify(verifier)) {
                throw new ForbiddenException("Signature mismatch");
            }

            return signed.getJWTClaimsSet().getSubject();

        } catch (Throwable cause) {
            throw new TokenException("Invalid access token", cause);
        }
    }

}
