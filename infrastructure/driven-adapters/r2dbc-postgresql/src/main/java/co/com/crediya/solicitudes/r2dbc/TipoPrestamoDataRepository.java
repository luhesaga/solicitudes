package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.TipoPrestamoData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TipoPrestamoDataRepository extends R2dbcRepository<TipoPrestamoData, Long> {
}
