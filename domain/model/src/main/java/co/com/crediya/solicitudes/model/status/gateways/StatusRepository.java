package co.com.crediya.solicitudes.model.status.gateways;

import co.com.crediya.solicitudes.model.status.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StatusRepository {
    Mono<Status> findById(Long id);

    Flux<Status> findAll();

    Mono<Status> findByName(String nombre);
}
