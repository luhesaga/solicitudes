package co.com.crediya.solicitudes.config;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import co.com.crediya.solicitudes.model.notification.gateways.NotificationGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.UpdateRequestStatus;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.model.status.Status;
import co.com.crediya.solicitudes.model.status.gateways.StatusRepository;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import co.com.crediya.solicitudes.usecase.list.ManualListRequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestValidator;
import co.com.crediya.solicitudes.usecase.updatestatus.UpdateRequestStatusUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void mustBuildAndUseUpdateRequestStatusUseCaseFromUseCasesConfig() {
        // Arrange
        RequestRepository requestRepository = Mockito.mock(RequestRepository.class);
        StatusRepository statusRepository = Mockito.mock(StatusRepository.class);
        NotificationGateway notificationGateway = Mockito.mock(NotificationGateway.class);
        UseCasesConfig config = new UseCasesConfig();

        UpdateRequestStatusUseCase useCase = config.updateRequestStatusUseCase(requestRepository, statusRepository, notificationGateway);

        assertNotNull(useCase, "No se pudo construir UpdateRequestStatusUseCase desde UseCasesConfig");

        Long requestId = 123L;
        String nextStatus = "APPROVED";
        UpdateRequestStatus command = UpdateRequestStatus.builder()
                .requestId(requestId)
                .status(nextStatus)
                .build();

        Status status = Status.builder().id(10L).name(nextStatus).build();
        Request existing = Request.builder().id(requestId).statusId(1L).build();
        Request updated = existing.toBuilder().statusId(status.getId()).build();

        when(statusRepository.findByName(nextStatus)).thenReturn(Mono.just(status));
        when(requestRepository.findById(requestId)).thenReturn(Mono.just(existing));
        when(requestRepository.update(Mockito.any())).thenReturn(Mono.just(updated));
        when(notificationGateway.send(Mockito.any())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertEquals(status.getId(), result.getStatusId()))
                .verifyComplete();

        verify(statusRepository).findByName(nextStatus);
        verify(requestRepository).findById(requestId);
        verify(requestRepository).update(Mockito.any());
        verify(notificationGateway).send(Mockito.any());
    }
}