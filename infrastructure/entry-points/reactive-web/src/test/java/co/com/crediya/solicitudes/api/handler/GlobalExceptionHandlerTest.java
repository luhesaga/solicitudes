package co.com.crediya.solicitudes.api.handler;

import co.com.crediya.solicitudes.api.dto.ErrorDTO;
import co.com.crediya.solicitudes.api.dto.ErrorMessages;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
        public String status() { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido"); }
        @GetMapping("/test/generic")
        public String generic() { throw new RuntimeException("boom"); }
        @GetMapping("/test/denied")
        public String denied() { throw new AuthorizationDeniedException("Denied"); }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenBusinessValidation_thenReturnsBadRequestWithMessage() {
        webTestClient.get()
                .uri("/test/business")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("400-001")
                .jsonPath("$.message").isEqualTo("Datos de entrada inválidos")
                .jsonPath("$.errors[0]").isEqualTo("Error de negocio");
    }

    @Test
    void whenResponseStatusException_thenReturnsBadRequestWithMessage() {
        webTestClient.get()
                .uri("/test/status")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorDTO.class)
                .value(err -> org.junit.jupiter.api.Assertions.assertEquals(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido").getMessage(),
                        err.getMessage()));
    }

    @Test
    void whenAccessDenied_thenReturnsForbiddenWithMessage() {
        webTestClient.get()
                .uri("/test/denied")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorDTO.class)
                .value(err -> org.junit.jupiter.api.Assertions.assertEquals(
                        ErrorMessages.ACCESS_DENIED, err.getMessage()));
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
