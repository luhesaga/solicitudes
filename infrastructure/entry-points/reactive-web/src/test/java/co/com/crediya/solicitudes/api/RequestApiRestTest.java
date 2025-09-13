package co.com.crediya.solicitudes.api;

import co.com.crediya.solicitudes.api.dto.RequestDTO;
import co.com.crediya.solicitudes.api.handler.GlobalExceptionHandler;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.usecase.list.ManualListRequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest(
        controllers = RequestApiRest.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class
)
@Import({GlobalExceptionHandler.class, co.com.crediya.solicitudes.api.mapper.RequestMapper.class})
class RequestApiRestTest {

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
        RequestUseCase requestUseCase() {
            return org.mockito.Mockito.mock(RequestUseCase.class);
        }

        @org.springframework.context.annotation.Bean
        ManualListRequestUseCase manualListRequestUseCase() {
            return org.mockito.Mockito.mock(ManualListRequestUseCase.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RequestUseCase requestUseCase;

    @Autowired
    private ManualListRequestUseCase manualListRequestUseCase;

    @Test
    @DisplayName("createRequest retorna 201 y GenericResponseDTO con la solicitud creada en data cuando la entrada es válida")
    void createRequest_ShouldReturn201WithValidBody() {
        RequestDTO dto = new RequestDTO(new BigDecimal("1500000.00"), 12, "user@example.com", 1L);

        Request respuesta = Request.builder()
                .id(123L)
                .amount(dto.getAmount())
                .term(dto.getTerm())
                .email(dto.getEmail())
                .loanTypeId(dto.getLoanTypeId())
                .statusId(1L)
                .build();
        when(requestUseCase.createRequest(any(Request.class), eq("user@example.com")))
                .thenReturn(Mono.just(respuesta));

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
                .jsonPath("$.code").isEqualTo("201-001")
                .jsonPath("$.message").isEqualTo("Operación exitosa.")
                .jsonPath("$.data.amount").isEqualTo(1500000.00)
                .jsonPath("$.data.term").isEqualTo(12)
                .jsonPath("$.data.email").isEqualTo("user@example.com")
                .jsonPath("$.data.loanTypeId").isEqualTo(1);
    }

    @Test
    @DisplayName("createRequest retorna 400 con ErrorDTO cuando el caso de uso lanza BusinessValidationException")
    void createRequest_ShouldReturn400WhenBusinessRuleFails() {
        RequestDTO dto = new RequestDTO(new BigDecimal("10.00"), 12, "user@example.com", 99L);
        when(requestUseCase.createRequest(any(Request.class), eq("user@example.com")))
                .thenReturn(Mono.error(new BusinessValidationException("Regla de negocio inválida")));

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
                .expectBody()
                .jsonPath("$.code").isEqualTo("400-001")
                .jsonPath("$.message").isEqualTo("Datos de entrada inválidos")
                .jsonPath("$.errors[0]").isEqualTo("Regla de negocio inválida");
    }

    @Test
    @DisplayName("manualRequestList retorna 200 y la lista paginada con RequestEnrichedDTO cuando el rol es ASESOR")
    void manualRequestList_ShouldReturn200WithList() {
        int page = 1;
        int size = 2;
        Request s1 = Request.builder().id(10L).email("a@b.com").term(6).amount(new BigDecimal("1000.00")).loanTypeId(2L).statusId(1L).build();
        Request s2 = Request.builder().id(11L).email("c@d.com").term(12).amount(new BigDecimal("2000.00")).loanTypeId(3L).statusId(1L).build();
        when(manualListRequestUseCase.list(page, size)).thenReturn(Flux.just(s1, s2));

        webTestClient
                .mutateWith(mockUser("asesor@example.com").roles("ASESOR"))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/solicitudes").queryParam("page", page).queryParam("size", size).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].email").isEqualTo("a@b.com")
                .jsonPath("$[0].amount").isEqualTo(1000.00)
                .jsonPath("$[1].email").isEqualTo("c@d.com")
                .jsonPath("$[1].amount").isEqualTo(2000.00);
    }
}
