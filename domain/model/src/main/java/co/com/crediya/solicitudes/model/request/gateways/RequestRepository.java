package co.com.crediya.solicitudes.model.request.gateways;

import co.com.crediya.solicitudes.model.request.Request;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RequestRepository {
    Mono<Request> save(Request request);
    Flux<Request> findForManualReview(int page, int size);
    Mono<Request> findById(Long id);
    Mono<Request> update(Request request);
    // Flux<Request> findByEmail(String email);
}
