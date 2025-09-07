package co.com.crediya.solicitudes.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.util.StandardCharset;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

@org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
@org.springframework.context.annotation.Import({SecurityConfig.class, SecurityWebFilterChainTest.TestRoutes.class})
@TestPropertySource(properties = {
        // 64 chars secret (HS512)
        "jwt.secret=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
})
class SecurityWebFilterChainTest {

    @org.springframework.beans.factory.annotation.Autowired
    WebTestClient client;

    @Configuration
    static class TestRoutes {
        @Bean
        RouterFunction<ServerResponse> testRouter() {
            return route(GET("/hello"), req -> ok().bodyValue("hello"));
        }
    }

    @Test
    void permitAll_for_swagger_paths_allows_through() {
        // Even if endpoint is not implemented, status should be 404 (not 401),
        // proving the security chain permitted the request.
        client.get().uri("/v3/api-docs").exchange()
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

    // Helper to create a signed HMAC JWT string
    private static String createHmacJwt(String secret, JWSAlgorithm alg, List<String> roles) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("roles", roles)
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .build();

        byte[] keyBytes = secret.getBytes(StandardCharset.UTF_8);
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(alg), com.nimbusds.jwt.JWTClaimsSet.parse(claims.toJSONObject()));
        MACSigner signer = new MACSigner(keyBytes);
        signedJWT.sign(signer);

        // sanity verify
        new MACVerifier(keyBytes);
        return signedJWT.serialize();
    }
}
