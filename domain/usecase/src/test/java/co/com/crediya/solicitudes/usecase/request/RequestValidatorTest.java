package co.com.crediya.solicitudes.usecase.request;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.loantype.LoanType;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestValidatorTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @InjectMocks
    private RequestValidator requestValidator;

    private Request validRequest;

    @BeforeEach
    void setUp() {
        validRequest = Request.builder()
                .amount(new BigDecimal("5000"))
                .term(12)
                .email("user@example.com")
                .loanTypeId(1L)
                .build();
    }

    @Test
    @DisplayName("entryDataValidation: deberia pasar con datos validos")
    void entryDataValidation_shouldPassWithValidData() {
        StepVerifier.create(requestValidator.entryDataValidation(validRequest))
                .expectNext(validRequest)
                .verifyComplete();
    }

    @Test
    @DisplayName("entryDataValidation: deberia fallar por email invalido")
    void entryDataValidation_shouldFailOnInvalidEmail() {
        Request withBadEmail = validRequest.toBuilder().email("invalido").build();

        StepVerifier.create(requestValidator.entryDataValidation(withBadEmail))
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof BusinessValidationException;
                    BusinessValidationException ex = (BusinessValidationException) throwable;
                    // Mensaje general de campos requeridos
                    assert Constants.ERROR_MESSAGE_REQUIRED_FIELDS.equals(ex.getMessage());
                    // Debe contener el error especifico de email invalido
                    assert ex.getErrors().contains(Constants.ERROR_INVALID_EMAIL_FORMAT);
                })
                .verify();
    }

    @Test
    @DisplayName("entryDataValidation: deberia fallar por monto o plazo invalidos")
    void entryDataValidation_shouldFailOnInvalidAmountOrTerm() {
        // monto <= 0
        Request amountZero = validRequest.toBuilder().amount(BigDecimal.ZERO).build();
        StepVerifier.create(requestValidator.entryDataValidation(amountZero))
                .expectErrorSatisfies(throwable -> {
                    BusinessValidationException ex = (BusinessValidationException) throwable;
                    assert Constants.ERROR_MESSAGE_REQUIRED_FIELDS.equals(ex.getMessage());
                    assert ex.getErrors().contains(Constants.ERROR_AMOUNT_OR_RATE_INVALID);
                })
                .verify();

        // plazo <= 0
        Request termZero = validRequest.toBuilder().term(0).build();
        StepVerifier.create(requestValidator.entryDataValidation(termZero))
                .expectErrorSatisfies(throwable -> {
                    BusinessValidationException ex = (BusinessValidationException) throwable;
                    assert Constants.ERROR_MESSAGE_REQUIRED_FIELDS.equals(ex.getMessage());
                    assert ex.getErrors().contains(Constants.ERROR_AMOUNT_OR_RATE_INVALID);
                })
                .verify();
    }

    @Test
    @DisplayName("loanTypeAndLimitsValidation: deberia fallar si no existe el tipo de prestamo")
    void loanTypeAndLimitsValidation_shouldFailWhenLoanTypeDoesNotExist() {
        when(loanTypeRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(requestValidator.loanTypeAndLimitsValidation(validRequest))
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof BusinessValidationException;
                    assert Constants.ERROR_LOAN_TYPE_DOES_NOT_EXIST.equals(throwable.getMessage());
                })
                .verify();
    }

    @Test
    @DisplayName("loanTypeAndLimitsValidation: deberia fallar cuando monto fuera de rango (menor al minimo)")
    void loanTypeAndLimitsValidation_shouldFailWhenAmountBelowMinimum() {
        LoanType loanType = LoanType.builder()
                .id(1L)
                .name("Personal")
                .minimumAmount(new BigDecimal("1000"))
                .maximunAmount(new BigDecimal("5000"))
                .interestRate(0.05)
                .automaticValidation(true)
                .build();
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        Request belowMin = validRequest.toBuilder().amount(new BigDecimal("999"))
                .loanTypeId(1L).build();

        StepVerifier.create(requestValidator.loanTypeAndLimitsValidation(belowMin))
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof BusinessValidationException;
                    assert Constants.ERROR_OUT_OF_RANGE_AMOUNT.equals(throwable.getMessage());
                })
                .verify();
    }

    @Test
    @DisplayName("loanTypeAndLimitsValidation: deberia fallar cuando monto fuera de rango (mayor al maximo)")
    void loanTypeAndLimitsValidation_shouldFailWhenAmountAboveMaximum() {
        LoanType loanType = LoanType.builder()
                .id(1L)
                .name("Personal")
                .minimumAmount(new BigDecimal("1000"))
                .maximunAmount(new BigDecimal("5000"))
                .interestRate(0.05)
                .automaticValidation(true)
                .build();
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        Request aboveMax = validRequest.toBuilder().amount(new BigDecimal("5001"))
                .loanTypeId(1L).build();

        StepVerifier.create(requestValidator.loanTypeAndLimitsValidation(aboveMax))
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof BusinessValidationException;
                    assert Constants.ERROR_OUT_OF_RANGE_AMOUNT.equals(throwable.getMessage());
                })
                .verify();
    }

    @Test
    @DisplayName("loanTypeAndLimitsValidation: deberia pasar cuando monto en el limite minimo y maximo")
    void loanTypeAndLimitsValidation_shouldPassOnBoundaryValues() {
        LoanType loanType = LoanType.builder()
                .id(1L)
                .name("Personal")
                .minimumAmount(new BigDecimal("1000"))
                .maximunAmount(new BigDecimal("5000"))
                .interestRate(0.05)
                .automaticValidation(true)
                .build();
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanType));

        Request atMin = validRequest.toBuilder().amount(new BigDecimal("1000")).loanTypeId(1L).build();
        Request atMax = validRequest.toBuilder().amount(new BigDecimal("5000")).loanTypeId(1L).build();

        StepVerifier.create(requestValidator.loanTypeAndLimitsValidation(atMin))
                .expectNext(atMin)
                .verifyComplete();

        StepVerifier.create(requestValidator.loanTypeAndLimitsValidation(atMax))
                .expectNext(atMax)
                .verifyComplete();
    }
}
