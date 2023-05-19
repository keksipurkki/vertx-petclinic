package net.keksipurkki.petstore.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

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

            var claims = signed.getJWTClaimsSet();

            if (!claims.getBooleanClaim("verifyMe")) {
                throw new SecurityException("Invalid token claims");
            }

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

            var claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("verifyMe", true)
                .expirationTime(new Date(new Date().getTime() + SESSION_TTL))
                .build();

            var jwt = new SignedJWT(header, claims);
            jwt.sign(signer);

            return new JwtPrincipal(jwt.serialize());

        } catch (Exception cause) {
            throw new IllegalStateException("Login token computation failed", cause);
        }
    }

}
