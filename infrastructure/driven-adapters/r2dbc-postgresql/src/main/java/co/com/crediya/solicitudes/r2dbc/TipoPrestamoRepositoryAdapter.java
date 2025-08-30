package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.tipoprestamo.TipoPrestamo;
import co.com.crediya.solicitudes.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.solicitudes.r2dbc.data.TipoPrestamoData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TipoPrestamoRepositoryAdapter implements TipoPrestamoRepository {
    private final TipoPrestamoDataRepository tipoPrestamoDataRepository;

    private TipoPrestamo toTipoPrestamoModel(TipoPrestamoData data) {
        return TipoPrestamo.builder()
                .idTipoPrestamo(data.getIdTipoPrestamo())
                .nombre(data.getNombre())
                .montoMinimo(data.getMontoMinimo())
                .montoMaximo(data.getMontoMaximo())
                .tasaInteres(data.getTasaInteres())
                .validacionAutomatica(data.getValidacionAutomatica())
                .build();
    }

    @Override
    public Mono<TipoPrestamo> findById(Long id) {
        return tipoPrestamoDataRepository.findById(id)
                .map(this::toTipoPrestamoModel);
    }
}
