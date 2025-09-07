package co.com.crediya.solicitudes.api;

import co.com.crediya.solicitudes.api.dto.ErrorDTO;
import co.com.crediya.solicitudes.api.dto.SolicitudDTO;
import co.com.crediya.solicitudes.api.handler.GlobalExceptionHandler;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest(
        controllers = ApiRest.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class
)
@Import({GlobalExceptionHandler.class})
class ApiRestTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class PrincipalTestConfig {
        @org.springframework.context.annotation.Bean
        org.springframework.web.server.WebFilter principalWebFilter() {
            return (exchange, chain) -> {
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "user@example.com",
                        "password",
                        org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_CLIENTE")
                );
                var mutated = exchange.mutate().principal(reactor.core.publisher.Mono.just(auth)).build();
                return chain.filter(mutated);
            };
        }

        @org.springframework.context.annotation.Bean
        co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase solicitudUseCase() {
            return org.mockito.Mockito.mock(co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SolicitudUseCase solicitudUseCase;

    @Test
    @DisplayName("crearSolicitud retorna 201 y la solicitud creada cuando la entrada es válida")
    void crearSolicitud_DeberiaRetornar201ConCuerpoValido() {
        // Arrange: DTO de entrada
        SolicitudDTO dto = new SolicitudDTO(new BigDecimal("1500000.00"), 12, "user@example.com", 1L);

        // y respuesta del caso de uso
        Solicitud respuesta = Solicitud.builder()
                .idSolicitud(123L)
                .monto(dto.getMonto())
                .plazo(dto.getPlazo())
                .email(dto.getEmail())
                .idTipoPrestamo(dto.getIdTipoPrestamo())
                .idEstado(1L)
                .build();
        when(solicitudUseCase.crearSolicitud(any(Solicitud.class), eq("user@example.com")))
                .thenReturn(Mono.just(respuesta));

        // Act & Assert
        webTestClient
                .mutateWith(mockUser("user@example.com").roles("CLIENTE"))
                .post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.idSolicitud").isEqualTo(123)
                .jsonPath("$.idEstado").isEqualTo(1)
                .jsonPath("$.monto").isEqualTo(1500000.00)
                .jsonPath("$.plazo").isEqualTo(12)
                .jsonPath("$.email").isEqualTo("user@example.com")
                .jsonPath("$.idTipoPrestamo").isEqualTo(1);
    }

    @Test
    @DisplayName("crearSolicitud retorna 400 con ErrorDTO cuando el caso de uso lanza BusinessValidationException")
    void crearSolicitud_DeberiaRetornar400CuandoReglaDeNegocioFalla() {
        // Arrange
        SolicitudDTO dto = new SolicitudDTO(new BigDecimal("10.00"), 12, "user@example.com", 99L);
        when(solicitudUseCase.crearSolicitud(any(Solicitud.class), eq("user@example.com")))
                .thenReturn(Mono.error(new BusinessValidationException("Regla de negocio inválida")));

        // Act & Assert
        webTestClient
                .mutateWith(mockUser("user@example.com").roles("CLIENTE"))
                .post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ErrorDTO.class)
                .value(err -> assertEquals("400 BAD_REQUEST \"Regla de negocio inválida\"", err.getMessage()));
    }
}
