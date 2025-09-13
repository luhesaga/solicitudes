package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.RequestData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface StatusDataRepository extends R2dbcRepository<RequestData, Long> {
    @Query("SELECT s.monto, s.plazo, s.email, " +
            "tp.nombre as \"nombreTipoPrestamo\", tp.tasa_interes as \"tasaInteres\", " +
            "e.nombre as \"nombreEstado\" " +
            "FROM solicitudes s " +
            "INNER JOIN tipos_prestamo tp ON s.id_tipo_prestamo = tp.id_tipo_prestamo " +
            "INNER JOIN estados e ON s.id_estado = e.id_estado " +
            "WHERE tp.validacion_automatica = false " +
            "ORDER BY s.fecha_creacion ASC " +
            "LIMIT :size OFFSET :offset")
    Flux<RequestData> findForManualReview(int size, int offset);
}
