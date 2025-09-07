package co.com.crediya.solicitudes.model.solicitud.gateways;

import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {
    Mono<Solicitud> guardarSolicitud(Solicitud solicitud);
    // En el futuro podríamos necesitar métodos como:
    // Mono<Solicitud> findById(Long id);
    // Flux<Solicitud> findByEmail(String email);
}
