package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.EstadoData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EstadoDataRepository extends R2dbcRepository<EstadoData, Long> {
    Mono<EstadoData> findByNombre(String nombre);
}
