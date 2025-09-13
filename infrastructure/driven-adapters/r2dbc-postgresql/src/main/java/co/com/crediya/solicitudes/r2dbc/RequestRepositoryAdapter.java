package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.r2dbc.data.RequestData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RequestRepositoryAdapter implements RequestRepository {

    private final StatusDataRepository statusDataRepository;
    private final ReactiveTransactionManager transactionManager;

    private Request toRequestModel(RequestData data) {
        return Request.builder()
                .id(data.getId())
                .amount(data.getAmount())
                .term(data.getTerm())
                .email(data.getEmail())
                .statusId(data.getStatusId())
                .loanTypeId(data.getLoanTypeId())
                .loanTypeName(data.getLoanTypeName())
                .interestRate(data.getInterestRate())
                .statusName(data.getStatusName())
                .build();
    }

    private RequestData toRequestData(Request model) {
        RequestData data = new RequestData();
        data.setId(model.getId());
        data.setAmount(model.getAmount());
        data.setTerm(model.getTerm());
        data.setEmail(model.getEmail());
        data.setStatusId(model.getStatusId());
        data.setLoanTypeId(model.getLoanTypeId());
        return data;
    }

    @Override
    public Mono<Request> save(Request request) {
        TransactionalOperator operator = TransactionalOperator.create(transactionManager); // operador transaccional reactivo
        log.trace("[R2DBCRepositoryAdapter] Guardando request con email={}", request.getEmail());
        return Mono.just(request)
                .map(this::toRequestData)
                .flatMap(statusDataRepository::save)
                .map(this::toRequestModel)
                .doOnSuccess(u -> log.debug("[R2DBCRepositoryAdapter] User guardado id={}, email={}", u.getId(), u.getEmail()))
                .doOnError(e -> log.warn("[R2DBCRepositoryAdapter] Error guardando usuario {}: {}", request.getEmail(), e.getMessage(), e))
                .as(operator::transactional); // <--- garantizar la atomicidad de la operación de guardado
    }

    @Override
    public Flux<Request> findForManualReview(int page, int size) {
        int offset = page * size;
        log.trace("[R2DBCRepositoryAdapter] Buscando solicitudes para revisión manual. Página: {}, Tamaño: {}", page, size);
        return statusDataRepository.findForManualReview(size, offset)
                .map(this::toRequestModel);
    }
}