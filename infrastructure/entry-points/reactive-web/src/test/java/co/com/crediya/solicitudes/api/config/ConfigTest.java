package co.com.crediya.solicitudes.api.config;

import co.com.crediya.solicitudes.api.ApiRest;
import co.com.crediya.solicitudes.api.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ContextConfiguration(classes = {ConfigTest.TestController.class, GlobalExceptionHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost"
})
class ConfigTest {

    @RestController
    static class TestController {
        @GetMapping("/api/usecase/path")
        public String ok() { return ""; }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/usecase/path")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}