package co.com.crediya.solicitudes.usecase.request;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestUseCaseTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestValidator requestValidator;

    @InjectMocks
    private RequestUseCase requestUseCase;

    private Request requestValidation;
    private String validAuthenticatedMail;

    @BeforeEach
    void setUp() {
        validAuthenticatedMail = "test@example.com";

        requestValidation = Request.builder()
                .amount(new BigDecimal("5000000.00"))
                .term(24)
                .email(validAuthenticatedMail)
                .loanTypeId(1L)
                .build();
    }

    @Test
    @DisplayName("Debería crear una solicitud exitosamente cuando todos los datos son válidos")
    void shouldCreateRequestSuccessfully() {
        when(requestValidator.entryDataValidation(any(Request.class))).thenReturn(Mono.just(requestValidation));
        when(requestValidator.loanTypeAndLimitsValidation(any(Request.class))).thenReturn(Mono.just(requestValidation));
        Request requestGuardada = requestValidation.toBuilder().id(100L).statusId(1L).build();
        when(requestRepository.save(any(Request.class))).thenReturn(Mono.just(requestGuardada));

        Mono<Request> result = requestUseCase.createRequest(requestValidation, validAuthenticatedMail);

        StepVerifier.create(result)
                .expectNextMatches(solicitud -> solicitud.getId() == 100L && solicitud.getStatusId() == 1L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el email autenticado no coincide con el de la solicitud")
    void shouldThrowErrorIfEmailDoesNotMatch() {
        String otroEmail = "atacante@example.com";

        Mono<Request> result = requestUseCase.createRequest(requestValidation, otroEmail);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().contains("No tiene permiso"))
                .verify();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el tipo de préstamo no existe")
    void shouldThrowErrorWhenLoanTypeDoesNotExist() {
        when(requestValidator.entryDataValidation(any(Request.class))).thenReturn(Mono.just(requestValidation));
        when(requestValidator.loanTypeAndLimitsValidation(any(Request.class)))
                .thenReturn(Mono.error(new BusinessValidationException(Constants.ERROR_LOAN_TYPE_DOES_NOT_EXIST)));

        Mono<Request> result = requestUseCase.createRequest(requestValidation, validAuthenticatedMail);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_LOAN_TYPE_DOES_NOT_EXIST))
                .verify();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el monto es menor al mínimo permitido")
    void shouldThrowErrorWhenAmountIsLessThanMinimum() {
        requestValidation.setAmount(new BigDecimal("500000.00"));
        when(requestValidator.entryDataValidation(any(Request.class))).thenReturn(Mono.just(requestValidation));
        when(requestValidator.loanTypeAndLimitsValidation(any(Request.class)))
                .thenReturn(Mono.error(new BusinessValidationException(Constants.ERROR_OUT_OF_RANGE_AMOUNT)));

        Mono<Request> result = requestUseCase.createRequest(requestValidation, validAuthenticatedMail);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_OUT_OF_RANGE_AMOUNT))
                .verify();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el monto es nulo o cero")
    void shouldThrowErrorWhenAmountIsInvalid() {
        requestValidation.setAmount(BigDecimal.ZERO);
        when(requestValidator.entryDataValidation(any(Request.class)))
                .thenReturn(Mono.error(new BusinessValidationException(Constants.ERROR_AMOUNT_OR_RATE_INVALID)));

        Mono<Request> result = requestUseCase.createRequest(requestValidation, validAuthenticatedMail);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_AMOUNT_OR_RATE_INVALID))
                .verify();
    }
}