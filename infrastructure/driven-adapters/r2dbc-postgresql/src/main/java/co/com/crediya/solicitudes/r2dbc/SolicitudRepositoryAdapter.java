package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.solicitudes.r2dbc.data.SolicitudData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SolicitudRepositoryAdapter implements SolicitudRepository {

    private final SolicitudDataRepository solicitudDataRepository;
    private final ReactiveTransactionManager transactionManager;

    // --- Mappers para Solicitud ---
    private Solicitud toSolicitudModel(SolicitudData data) {
        return Solicitud.builder()
                .idSolicitud(data.getIdSolicitud())
                .monto(data.getMonto())
                .plazo(data.getPlazo())
                .email(data.getEmail())
                .idEstado(data.getIdEstado())
                .idTipoPrestamo(data.getIdTipoPrestamo())
                .build();
    }

    private SolicitudData toSolicitudData(Solicitud model) {
        SolicitudData data = new SolicitudData();
        data.setIdSolicitud(model.getIdSolicitud());
        data.setMonto(model.getMonto());
        data.setPlazo(model.getPlazo());
        data.setEmail(model.getEmail());
        data.setIdEstado(model.getIdEstado());
        data.setIdTipoPrestamo(model.getIdTipoPrestamo());
        return data;
    }

    // --- Implementación de los métodos del contrato ---

    @Override
    public Mono<Solicitud> guardarSolicitud(Solicitud solicitud) {
        TransactionalOperator operator = TransactionalOperator.create(transactionManager); // operador transaccional reactivo
        log.trace("[R2DBCRepositoryAdapter] Guardando solicitud con email={}", solicitud.getEmail());
        return Mono.just(solicitud)
                .map(this::toSolicitudData)
                .flatMap(solicitudDataRepository::save)
                .map(this::toSolicitudModel)
                .doOnSuccess(u -> log.debug("[R2DBCRepositoryAdapter] Usuario guardado id={}, email={}", u.getIdSolicitud(), u.getEmail()))
                .doOnError(e -> log.warn("[R2DBCRepositoryAdapter] Error guardando usuario {}: {}", solicitud.getEmail(), e.getMessage(), e))
                .as(operator::transactional); // <--- garantizar la atomicidad de la operación de guardado
    }
}
