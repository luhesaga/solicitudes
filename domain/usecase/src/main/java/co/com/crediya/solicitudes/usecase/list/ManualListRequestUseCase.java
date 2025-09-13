package co.com.crediya.solicitudes.usecase.list;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import co.com.crediya.solicitudes.model.util.Constants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
public class ManualListRequestUseCase {

    private final RequestRepository requestRepository;
    private final UserGateway userGateway;
    private final LoggerGateway logger;

    public Flux<Request> list(int page, int size) {
        return requestRepository.findForManualReview(page, size)
                .flatMap(this::enrichWithUserData);
    }

    private Mono<Request> enrichWithUserData(Request request) {

        logger.info("Enriqueciendo request para el email: {}", request.getEmail());
        return userGateway.findByEmail(request.getEmail())
                .map(user -> {
                    logger.info("User encontrado para {}: {}", request.getEmail(), user.getName());

                    BigDecimal monthlyAmount = BigDecimal.ZERO;
                    if (request.getAmount() != null && request.getInterestRate() != null && request.getTerm() != null && request.getTerm() > 0) {
                        BigDecimal decimalRate = BigDecimal.valueOf(request.getInterestRate() / 100.0);
                        BigDecimal rateTotal = request.getAmount().multiply(decimalRate);
                        BigDecimal amountTotal = request.getAmount().add(rateTotal);
                        monthlyAmount = amountTotal.divide(BigDecimal.valueOf(request.getTerm()), 2, RoundingMode.HALF_UP);
                    }

                    return request.toBuilder()
                            .userName(user.getName() + " " + user.getLastname())
                            .salary(user.getSalary())
                            .approximateMonthlyAmount(monthlyAmount)
                            .build();
                })
                .doOnSuccess(s -> {
                    if (s == null || s.getUserName() == null) {
                        logger.warn(Constants.ERROR_USER_NOT_FOUND, request.getEmail());
                    }
                })
                .onErrorResume(ex -> {
                    logger.error(Constants.ERROR_REQUEST, ex, request.getEmail());
                    return Mono.just(request);
                })
                .defaultIfEmpty(request);
    }
}
