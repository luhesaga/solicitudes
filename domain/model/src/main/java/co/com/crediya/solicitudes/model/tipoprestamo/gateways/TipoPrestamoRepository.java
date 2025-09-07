package co.com.crediya.solicitudes.model.tipoprestamo.gateways;

import co.com.crediya.solicitudes.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> findById(Long id);
}
