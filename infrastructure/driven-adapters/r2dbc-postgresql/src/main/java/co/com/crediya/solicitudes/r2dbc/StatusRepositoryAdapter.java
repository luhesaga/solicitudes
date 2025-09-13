package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.status.Status;
import co.com.crediya.solicitudes.model.status.gateways.StatusRepository;
import co.com.crediya.solicitudes.r2dbc.data.StatusData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class StatusRepositoryAdapter implements StatusRepository {
    private final EstadoDataRepository estadoDataRepository;

    private Status toStatusModel(StatusData data) {
        return new Status(data.getId(), data.getName(), data.getDescription());
    }

    @Override
    public Mono<Status> findById(Long id) {
        return estadoDataRepository.findById(id)
                .map(this::toStatusModel);
    }

    @Override
    public Flux<Status> findAll() {
        return estadoDataRepository.findAll()
                .map(this::toStatusModel);
    }

    @Override
    public Mono<Status> findByName(String nombre) {
        return estadoDataRepository.findByName(nombre)
                .map(this::toStatusModel);
    }
}
