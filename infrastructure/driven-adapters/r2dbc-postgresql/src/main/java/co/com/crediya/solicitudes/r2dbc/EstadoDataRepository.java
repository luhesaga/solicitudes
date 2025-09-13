package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.StatusData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EstadoDataRepository extends R2dbcRepository<StatusData, Long> {
    Mono<StatusData> findByName(String nombre);
}
