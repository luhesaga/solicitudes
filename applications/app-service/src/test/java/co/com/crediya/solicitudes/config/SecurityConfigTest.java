package co.com.crediya.solicitudes.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import java.nio.charset.StandardCharsets;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private static final String SECRET = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 64 chars

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void reactiveJwtDecoder_acceptsHS512Token() throws JOSEException, ParseException {
        ReactiveJwtDecoder decoder = config.reactiveJwtDecoder(SECRET);

        String token = createHmacJwt(SECRET, JWSAlgorithm.HS512, List.of("ROLE_USER", "ADMIN"));

        var jwt = decoder.decode(token).block();
        assertNotNull(jwt, "Decoded JWT should not be null");
        assertEquals(JWSAlgorithm.HS512.getName(), jwt.getHeaders().get("alg"));
    }

    @Test
    void reactiveJwtDecoder_rejectsHS256Token() throws JOSEException, ParseException {
        ReactiveJwtDecoder decoder = config.reactiveJwtDecoder(SECRET);

        String token = createHmacJwt(SECRET, JWSAlgorithm.HS256, List.of("ROLE_USER"));

        assertThrows(RuntimeException.class, () -> decoder.decode(token).block());
    }

    @Test
    void jwtAuthenticationConverter_mapsRolesToAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .claim("roles", List.of("ROLE_USER", "ADMIN"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var converter = config.jwtAuthenticationConverter();
        Mono<? extends AbstractAuthenticationToken> monoAuth = converter.convert(jwt);
        assertNotNull(monoAuth);
        AbstractAuthenticationToken auth = monoAuth.block();

        assertNotNull(auth, "Authentication should not be null");
        Set<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ADMIN"));
        assertEquals(2, authorities.size());
    }

    private static String createHmacJwt(String secret, JWSAlgorithm alg, List<String> roles) throws JOSEException, ParseException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("roles", roles)
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .build();

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(alg), com.nimbusds.jwt.JWTClaimsSet.parse(claims.toJSONObject()));
        MACSigner signer = new MACSigner(keyBytes);
        signedJWT.sign(signer);

        assertTrue(signedJWT.verify(new MACVerifier(keyBytes)));

        return signedJWT.serialize();
    }

    @Test
    void jwtAuthenticationConverter_missingRolesClaim_resultsInNoAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var converter = config.jwtAuthenticationConverter();
        AbstractAuthenticationToken auth = Objects.requireNonNull(converter.convert(jwt)).block();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
    }

    @Test
    void jwtAuthenticationConverter_emptyRoles_resultsInNoAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .claim("roles", List.of())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        var converter = config.jwtAuthenticationConverter();
        AbstractAuthenticationToken auth = Objects.requireNonNull(converter.convert(jwt)).block();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
    }

    @Test
    void reactiveJwtDecoder_invalidSignatureWithWrongSecret_fails() throws Exception {
        ReactiveJwtDecoder decoder = config.reactiveJwtDecoder(SECRET);
        String tokenSignedWithOtherSecret = createHmacJwt(
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                JWSAlgorithm.HS512,
                List.of("ROLE_USER")
        );
        assertThrows(RuntimeException.class, () -> decoder.decode(tokenSignedWithOtherSecret).block());
    }

    @Test
    void reactiveJwtDecoder_expiredToken_fails() throws Exception {
        ReactiveJwtDecoder decoder = config.reactiveJwtDecoder(SECRET);
        String expiredToken = createExpiredHmacJwt(SECRET, JWSAlgorithm.HS512, List.of("ROLE_USER"));
        assertThrows(RuntimeException.class, () -> decoder.decode(expiredToken).block());
    }

    private static String createExpiredHmacJwt(String secret, JWSAlgorithm alg, List<String> roles) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("roles", roles)
                .issueTime(java.util.Date.from(Instant.now().minusSeconds(7200)))
                .expirationTime(java.util.Date.from(Instant.now().minusSeconds(3600)))
                .build();
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(alg), com.nimbusds.jwt.JWTClaimsSet.parse(claims.toJSONObject()));
        MACSigner signer = new MACSigner(keyBytes);
        signedJWT.sign(signer);
        assertTrue(signedJWT.verify(new MACVerifier(keyBytes)));
        return signedJWT.serialize();
    }
}
