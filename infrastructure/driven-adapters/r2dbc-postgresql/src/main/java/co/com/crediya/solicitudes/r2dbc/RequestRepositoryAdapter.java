package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.r2dbc.mapper.RequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.crediya.solicitudes.r2dbc.mapper.RequestMapper.toRequestData;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RequestRepositoryAdapter implements RequestRepository {

    private final RequestDataRepository requestDataRepository;
    private final ReactiveTransactionManager transactionManager;



    @Override
    public Mono<Request> save(Request request) {
        TransactionalOperator operator = TransactionalOperator.create(transactionManager); // operador transaccional reactivo
        log.trace("[R2DBCRepositoryAdapter] Guardando request con email={}", request.getEmail());
        return Mono.just(request)
                .map(RequestMapper::toRequestData)
                .flatMap(requestDataRepository::save)
                .map(RequestMapper::toRequestModel)
                .doOnSuccess(u -> log.debug("[R2DBCRepositoryAdapter] User guardado id={}, email={}", u.getId(), u.getEmail()))
                .doOnError(e -> log.warn("[R2DBCRepositoryAdapter] Error guardando usuario {}: {}", request.getEmail(), e.getMessage(), e))
                .as(operator::transactional); // <--- garantizar la atomicidad de la operación de guardado
    }

    @Override
    public Flux<Request> findForManualReview(int page, int size) {
        int offset = page * size;
        log.trace("[R2DBCRepositoryAdapter] Buscando solicitudes para revisión manual. Página: {}, Tamaño: {}", page, size);
        return requestDataRepository.findForManualReview(size, offset)
                .map(RequestMapper::toRequestModel);
    }

    @Override
    public Mono<Request> findById(Long id) {
        return requestDataRepository.findById(id)
                .map(RequestMapper::toRequestModel);
    }

    @Override
    public Mono<Request> update(Request request) {
        return requestDataRepository.save(toRequestData(request))
                .map(RequestMapper::toRequestModel);
    }
}