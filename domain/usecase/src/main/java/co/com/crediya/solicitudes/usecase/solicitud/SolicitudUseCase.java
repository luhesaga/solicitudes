package co.com.crediya.solicitudes.usecase.solicitud;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.solicitudes.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.solicitudes.model.util.Constants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class SolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    // NOTA: Aún no necesitamos el EstadoRepository, lo usaremos en futuras HU.

    // Expresión regular para una validación básica de email.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public Mono<Solicitud> crearSolicitud(Solicitud solicitud) {
        return Mono.just(solicitud)
                .flatMap(this::validarDatosEntrada)
                .flatMap(this::validarTipoPrestamoYLimites)
                .map(solicitudValidada -> {
                    // Asignamos el estado inicial directamente.
                    // En un escenario real, buscaríamos el ID del estado "RECIBIDA".
                    // Por simplicidad para la HU2, asumimos que el ID del estado inicial es 1.
                    return solicitudValidada.toBuilder().idEstado(1L).build();
                })
                .flatMap(solicitudRepository::guardarSolicitud);
    }

    private Mono<Solicitud> validarDatosEntrada(Solicitud solicitud) {
        // Validación del formato del email
        if (solicitud.getEmail() == null || solicitud.getEmail().isBlank() || !EMAIL_PATTERN.matcher(solicitud.getEmail()).matches()) {
            return Mono.error(new BusinessValidationException(Constants.ERROR_FORMATO_EMAIL_INVALIDO));
        }
        if (solicitud.getMonto() == null || solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0 ||
                solicitud.getPlazo() == null || solicitud.getPlazo() <= 0) {
            return Mono.error(new BusinessValidationException(Constants.ERROR_MONTO_O_PLAZO_INVALIDO));
        }
        return Mono.just(solicitud);
    }

    private Mono<Solicitud> validarTipoPrestamoYLimites(Solicitud solicitud) {
        return tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new BusinessValidationException(Constants.ERROR_TIPO_PRESTAMO_NO_EXISTE)))
                .flatMap(tipoPrestamo -> {
                    BigDecimal monto = solicitud.getMonto();
                    if (monto.compareTo(tipoPrestamo.getMontoMinimo()) < 0 || monto.compareTo(tipoPrestamo.getMontoMaximo()) > 0) {
                        return Mono.error(new BusinessValidationException(Constants.ERROR_MONTO_FUERA_DE_RANGO));
                    }
                    return Mono.just(solicitud);
                });
    }
}
