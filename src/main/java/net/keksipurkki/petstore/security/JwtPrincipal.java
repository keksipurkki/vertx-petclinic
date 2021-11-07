package net.keksipurkki.petstore.security;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.api.UnexpectedApiException;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JwtPrincipal implements Principal {

    private static final byte[] SECRET = "A".repeat(32).getBytes(StandardCharsets.UTF_8);
    private static final long SESSION_TTL = TimeUnit.MINUTES.toMillis(15);

    private final String name;
    private final String jwt;

    public JwtPrincipal(String jwt) throws SecurityException {

        JWSVerifier verifier;
        SignedJWT signed;

        try {
            verifier = new MACVerifier(SECRET);
            signed = SignedJWT.parse(jwt);
        } catch (Throwable cause) {
            throw new SecurityException("Malformed token", cause);
        }

        try {

            if (!signed.verify(verifier)) {
                throw new SecurityException("Invalid signature");
            }

            // TODO: JWT Claims Verification...

            this.jwt = jwt;
            this.name = signed.getJWTClaimsSet().getSubject();

        } catch (JOSEException | ParseException cause) {
            throw new SecurityException("Invalid principal", cause);
        }

    }

    @Override
    public String getName() {
        return name;
    }

    public String getToken() {
        return jwt;
    }

    public static JwtPrincipal from(String subject) {
        try {

            var signer = new MACSigner(SECRET);
            var header = new JWSHeader(JWSAlgorithm.HS256);

            var claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://keksipurkki.net")
                .expirationTime(new Date(new Date().getTime() + SESSION_TTL))
                .build();

            var jwt = new SignedJWT(header, claimsSet);
            jwt.sign(signer);

            return new JwtPrincipal(jwt.serialize());

        } catch (Exception cause) {
            throw new UnexpectedApiException("Login token computation failed", cause);
        }
    }

}
