package co.com.crediya.solicitudes.usecase.request;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.model.util.Constants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class RequestUseCase {
    private final RequestRepository requestRepository;
    private final RequestValidator requestValidator;

    public Mono<Request> createRequest(Request request, String emailAutenticado) {
        if (!emailAutenticado.equals(request.getEmail())) {
            return Mono.error(new BusinessValidationException(Constants.ERROR_UNAUTHENTICATED_EMAIL));
        }
        return requestValidator.entryDataValidation(request)
                .flatMap(requestValidator::loanTypeAndLimitsValidation)
                .map(validatedRequest -> validatedRequest.toBuilder().statusId(1L).build())
                .flatMap(requestRepository::save);
    }

}
