package co.com.crediya.solicitudes.usecase.request;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.util.Constants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RequestValidator {

    private final RequestRepository requestRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<Request> entryDataValidation(Request request) {
        List<String> errors = new ArrayList<>();

        requiredFieldsValidation(request, errors);

        if (!errors.isEmpty()) {
            return Mono.error(new BusinessValidationException(Constants.ERROR_MESSAGE_REQUIRED_FIELDS, errors));
        }

        return Mono.just(request);
    }

    public Mono<Request> loanTypeAndLimitsValidation(Request request) {
        return loanTypeRepository.findById(request.getLoanTypeId())
                .switchIfEmpty(Mono.error(new BusinessValidationException(Constants.ERROR_LOAN_TYPE_DOES_NOT_EXIST)))
                .flatMap(tipoPrestamo -> {
                    BigDecimal monto = request.getAmount();
                    if (monto.compareTo(tipoPrestamo.getMinimumAmount()) < 0 || monto.compareTo(tipoPrestamo.getMaximunAmount()) > 0) {
                        return Mono.error(new BusinessValidationException(Constants.ERROR_OUT_OF_RANGE_AMOUNT));
                    }
                    return Mono.just(request);
                });
    }

    private void requiredFieldsValidation(Request request, List<String> errors) {

        if (request.getEmail() == null || request.getEmail().isBlank() || !Constants.EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            errors.add(Constants.ERROR_INVALID_EMAIL_FORMAT);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0 ||
                request.getTerm() == null || request.getTerm() <= 0) {
            errors.add(Constants.ERROR_AMOUNT_OR_RATE_INVALID);
        }

    }
}
