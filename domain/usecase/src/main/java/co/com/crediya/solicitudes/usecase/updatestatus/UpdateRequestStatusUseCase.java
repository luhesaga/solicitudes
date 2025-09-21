package co.com.crediya.solicitudes.usecase.updatestatus;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.notification.gateways.NotificationGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.UpdateRequestStatus;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.status.gateways.StatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateRequestStatusUseCase {

    private final RequestRepository requestRepository;
    private final StatusRepository statusRepository;
    private final NotificationGateway notificationGateway;

    public Mono<Request> execute(UpdateRequestStatus command) {
        return statusRepository.findByName(command.getStatus())
                .switchIfEmpty(Mono.error(new BusinessValidationException("El estado especificado no es válido.")))
                .flatMap(newStatus ->
                        requestRepository.findById(command.getRequestId())
                                .switchIfEmpty(Mono.error(new BusinessValidationException("La solicitud no fue encontrada.")))
                                .flatMap(request -> {
                                    request.setStatusId(newStatus.getId());

                                    return requestRepository.update(request);
                                })
                )
                .flatMap(updatedRequest ->
                        notificationGateway.send(updatedRequest)
                                .thenReturn(updatedRequest)
                );
    }
}
