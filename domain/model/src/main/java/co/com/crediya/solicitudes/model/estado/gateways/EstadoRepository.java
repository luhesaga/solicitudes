package co.com.crediya.solicitudes.model.estado.gateways;

import co.com.crediya.solicitudes.model.estado.Estado;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EstadoRepository {
    Mono<Estado> findById(Long id);

    Flux<Estado> findAll();

    Mono<Estado> findByNombre(String nombre);
}
