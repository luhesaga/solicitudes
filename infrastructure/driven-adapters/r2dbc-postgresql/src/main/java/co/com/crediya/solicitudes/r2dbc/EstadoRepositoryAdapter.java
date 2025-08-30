package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.estado.Estado;
import co.com.crediya.solicitudes.model.estado.gateways.EstadoRepository;
import co.com.crediya.solicitudes.r2dbc.data.EstadoData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class EstadoRepositoryAdapter implements EstadoRepository {
    private final EstadoDataRepository estadoDataRepository;

    private Estado toEstadoModel(EstadoData data) {
        return new Estado(data.getIdEstado(), data.getNombre(), data.getDescripcion());
    }

    @Override
    public Mono<Estado> findById(Long id) {
        return estadoDataRepository.findById(id)
                .map(this::toEstadoModel);
    }

    @Override
    public Flux<Estado> findAll() {
        return estadoDataRepository.findAll()
                .map(this::toEstadoModel);
    }

    @Override
    public Mono<Estado> findByNombre(String nombre) {
        return estadoDataRepository.findByNombre(nombre)
                .map(this::toEstadoModel);
    }
}
