package co.com.crediya.solicitudes.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import java.nio.charset.StandardCharsets;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Instant;
import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest(useDefaultFilters = false)
@org.springframework.context.annotation.Import({SecurityConfig.class, SecurityWebFilterChainTest.TestRoutes.class})
@TestPropertySource(properties = {
        "jwt.secret=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "SERVER_PORT=0",
        "ADAPTERS_R2DBC_HOST=localhost",
        "ADAPTERS_R2DBC_DATABASE=testdb",
        "ADAPTERS_R2DBC_USERNAME=user",
        "ADAPTERS_R2DBC_PASSWORD=pass",
        "JWT_SECRET=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "JWT_EXPIRATION_MS=86400000",
        "AWS_QUEUE_URL=http://localhost:4566/000000000000/test-queue",
        "AWS_ACCESS_KEY_ID=dummy",
        "AWS_SECRET_ACCESS_KEY=dummy",
        "spring.profiles.include="
})
class SecurityWebFilterChainTest {

    @org.springframework.beans.factory.annotation.Autowired
    WebTestClient client;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> testRouter() {
            return route(GET("/hello"), req -> ok().bodyValue("hello"));
        }
    }

    @Test
    void permitAll_for_swagger_paths_allows_through() {
        // Use a path that definitely matches the permitted pattern "/v3/api-docs/**".
        // Expect 404 (not 401/403) to verify the request bypasses security and reaches routing.
        client.get().uri("/v3/api-docs/swagger-config").exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void any_other_exchange_requires_authentication() {
        client.get().uri("/hello").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authenticated_request_with_valid_jwt_succeeds() throws Exception {
        String token = createHmacJwt(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                JWSAlgorithm.HS512,
                List.of("ROLE_USER")
        );

        client.get().uri("/hello")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("hello");
    }

    private static String createHmacJwt(String secret, JWSAlgorithm alg, List<String> roles) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("roles", roles)
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .build();

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(alg), com.nimbusds.jwt.JWTClaimsSet.parse(claims.toJSONObject()));
        MACSigner signer = new MACSigner(keyBytes);
        signedJWT.sign(signer);

        if (!signedJWT.verify(new MACVerifier(keyBytes))) {
            throw new IllegalStateException("JWT signature verification failed");
        }
        return signedJWT.serialize();
    }
}
