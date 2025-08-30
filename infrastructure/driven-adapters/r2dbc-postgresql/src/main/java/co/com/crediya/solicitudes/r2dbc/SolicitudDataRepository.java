package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.SolicitudData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface SolicitudDataRepository extends R2dbcRepository<SolicitudData, Long> {
}
