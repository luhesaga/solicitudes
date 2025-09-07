package co.com.crediya.solicitudes.api.handler;

import co.com.crediya.solicitudes.api.dto.ErrorDTO;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandler.class
})
@WebFluxTest(excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
@Import({co.com.crediya.solicitudes.api.config.CorsConfig.class,
        co.com.crediya.solicitudes.api.config.SecurityHeadersConfig.class})
public class GlobalExceptionHandlerTest {

    @RestController
    static class TestController {
        @GetMapping("/test/business")
        public String business() { throw new BusinessValidationException("Error de negocio"); }
        @GetMapping("/test/status")
        public String status() { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido"); }
        @GetMapping("/test/generic")
        public String generic() { throw new RuntimeException("boom"); }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenBusinessValidation_thenReturnsBadRequestWithMessage() {
        webTestClient.get()
                .uri("/test/business")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorDTO.class)
                .value(err -> org.junit.jupiter.api.Assertions.assertEquals("Error de negocio", err.getMessage()));
    }

    @Test
    void whenResponseStatusException_thenReturnsBadRequestWithMessage() {
        webTestClient.get()
                .uri("/test/status")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorDTO.class)
                .value(err -> org.junit.jupiter.api.Assertions.assertEquals(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido").getMessage(),
                        err.getMessage()));
    }

    @Test
    void whenGenericException_thenReturnsInternalServerErrorWithGenericMessage() {
        webTestClient.get()
                .uri("/test/generic")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorDTO.class)
                .value(err -> org.junit.jupiter.api.Assertions.assertEquals(
                        "Ha ocurrido un error inesperado. Por favor, contacte al soporte.", err.getMessage()));
    }
}
