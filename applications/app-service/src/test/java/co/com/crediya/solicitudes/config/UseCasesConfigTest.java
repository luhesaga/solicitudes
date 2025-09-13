package co.com.crediya.solicitudes.config;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import co.com.crediya.solicitudes.usecase.list.ManualListRequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UseCasesConfigTest {

    @Test
    void mustBuildAndUseRequestUseCaseFromUseCasesConfig() {
        RequestRepository requestRepository = org.mockito.Mockito.mock(RequestRepository.class);
        LoanTypeRepository loanTypeRepository = org.mockito.Mockito.mock(LoanTypeRepository.class);
        UseCasesConfig config = new UseCasesConfig();

        RequestValidator validator = config.requestValidator(requestRepository, loanTypeRepository);

        RequestUseCase useCase = config.requestUseCase(requestRepository, validator);

        assertNotNull(useCase, "No se pudo construir RequestUseCase desde UseCasesConfig");

        Request requestInvalida = Request.builder()
                .email("correo-invalido")
                .build();

        StepVerifier.create(useCase.createRequest(requestInvalida, "correo-invalido"))
                .expectError()
                .verify();
    }

    @Test
    void mustBuildAndUseManualListRequestUseCaseFromUseCasesConfig() {
        RequestRepository requestRepository = Mockito.mock(RequestRepository.class);
        UserGateway userGateway = Mockito.mock(UserGateway.class);
        LoggerGateway loggerGateway = Mockito.mock(LoggerGateway.class);
        UseCasesConfig config = new UseCasesConfig();
        int page = 0;
        int size = 10;
        when(requestRepository.findForManualReview(page, size)).thenReturn(Flux.empty());

        ManualListRequestUseCase listUseCase = config.manualListRequestUseCase(requestRepository, userGateway, loggerGateway);

        assertNotNull(listUseCase, "No se pudo construir ManualListRequestUseCase desde UseCasesConfig");

        StepVerifier.create(listUseCase.list(page, size))
                .verifyComplete();
        verify(requestRepository).findForManualReview(page, size);
    }
}