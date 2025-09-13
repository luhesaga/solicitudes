package co.com.crediya.solicitudes.usecase.list;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.user.User;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManualListRequestUseCaseTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserGateway userGateway;

    @Mock
    private LoggerGateway loggerGateway;

    private ManualListRequestUseCase manualListRequestUseCase;

    private Request s1;
    private Request s2;

    @BeforeEach
    void setUp() {
        manualListRequestUseCase = new ManualListRequestUseCase(requestRepository, userGateway, loggerGateway);

        s1 = Request.builder()
                .id(1L)
                .amount(new BigDecimal("1000000.00"))
                .term(12)
                .email("user1@example.com")
                .statusId(2L)
                .loanTypeId(1L)
                .interestRate(10.0)
                .build();

        s2 = Request.builder()
                .id(2L)
                .amount(new BigDecimal("2500000.00"))
                .term(24)
                .email("user2@example.com")
                .statusId(2L)
                .loanTypeId(1L)
                .interestRate(12.0)
                .build();
    }

    @Test
    @DisplayName("Debería list solicitudes para revisión manual usando el repositorio (happy path)")
    void listRequestHappyPath() {
        int page = 0;
        int size = 2;
        when(requestRepository.findForManualReview(page, size)).thenReturn(Flux.just(s1, s2));
        when(userGateway.findByEmail("user1@example.com")).thenReturn(Mono.just(User.builder().name("John").lastname("Doe").salary(new BigDecimal("3000000")).build()));
        when(userGateway.findByEmail("user2@example.com")).thenReturn(Mono.just(User.builder().name("Jane").lastname("Roe").salary(new BigDecimal("4500000")).build()));

        Flux<Request> result = manualListRequestUseCase.list(page, size);

        StepVerifier.create(result)
                .assertNext(r -> {
                    assertEquals("user1@example.com", r.getEmail());
                    assertEquals("John Doe", r.getUserName());
                })
                .assertNext(r -> {
                    assertEquals("user2@example.com", r.getEmail());
                    assertEquals("Jane Roe", r.getUserName());
                })
                .verifyComplete();

        verify(requestRepository, times(1)).findForManualReview(page, size);
        verify(userGateway, times(1)).findByEmail("user1@example.com");
        verify(userGateway, times(1)).findByEmail("user2@example.com");
        verifyNoMoreInteractions(requestRepository);
    }

    @Test
    @DisplayName("Debería propagar el error del repositorio")
    void requestListPropagateError() {
        int page = 1;
        int size = 10;
        RuntimeException boom = new RuntimeException("Fallo al consultar");
        when(requestRepository.findForManualReview(page, size)).thenReturn(Flux.error(boom));

        StepVerifier.create(manualListRequestUseCase.list(page, size))
                .expectErrorMatches(t -> t == boom || (t instanceof RuntimeException && t.getMessage().contains("Fallo")))
                .verify();

        verify(requestRepository).findForManualReview(page, size);
    }

    @Test
    @DisplayName("Debería devolver vacío cuando no hay solicitudes para revisión manual")
    void emptyRequestList() {
        int page = 0;
        int size = 5;
        when(requestRepository.findForManualReview(page, size)).thenReturn(Flux.empty());

        StepVerifier.create(manualListRequestUseCase.list(page, size))
                .verifyComplete();

        verify(requestRepository).findForManualReview(page, size);
    }
}
