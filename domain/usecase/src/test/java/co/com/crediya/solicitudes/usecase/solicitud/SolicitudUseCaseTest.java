package co.com.crediya.solicitudes.usecase.solicitud;

import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.solicitudes.model.tipoprestamo.TipoPrestamo;
import co.com.crediya.solicitudes.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.solicitudes.model.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class SolicitudUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud solicitudValida;
    private TipoPrestamo tipoPrestamoValido;

    @BeforeEach
    void setUp() {
        // Objeto base para un tipo de préstamo válido
        tipoPrestamoValido = TipoPrestamo.builder()
                .idTipoPrestamo(1L)
                .nombre("Libre Inversión")
                .montoMinimo(new BigDecimal("1000000.00"))
                .montoMaximo(new BigDecimal("50000000.00"))
                .build();

        // Objeto base para una solicitud válida que cumple con los rangos del tipo de préstamo
        solicitudValida = Solicitud.builder()
                .monto(new BigDecimal("5000000.00"))
                .plazo(24)
                .email("test@example.com")
                .idTipoPrestamo(1L)
                .build();
    }

    @Test
    @DisplayName("Debería crear una solicitud exitosamente cuando todos los datos son válidos")
    void deberiaCrearSolicitudExitosamente() {
        // Arrange
        // Simulamos que el tipo de préstamo existe
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.just(tipoPrestamoValido));

        // Simulamos que el guardado en la BD es exitoso
        Solicitud solicitudGuardada = solicitudValida.toBuilder().idSolicitud(100L).idEstado(1L).build();
        when(solicitudRepository.guardarSolicitud(any(Solicitud.class))).thenReturn(Mono.just(solicitudGuardada));

        // Act
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida);

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(solicitud -> solicitud.getIdSolicitud() == 100L && solicitud.getIdEstado() == 1L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el tipo de préstamo no existe")
    void deberiaLanzarErrorCuandoTipoPrestamoNoExiste() {
        // Arrange
        // Simulamos que el tipo de préstamo NO se encuentra en la BD
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_TIPO_PRESTAMO_NO_EXISTE))
                .verify();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el monto es menor al mínimo permitido")
    void deberiaLanzarErrorCuandoMontoEsMenorAlMinimo() {
        // Arrange
        solicitudValida.setMonto(new BigDecimal("500000.00")); // Monto por debajo del millón
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.just(tipoPrestamoValido));

        // Act
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_MONTO_FUERA_DE_RANGO))
                .verify();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el monto es nulo o cero")
    void deberiaLanzarErrorCuandoMontoEsInvalido() {
        // Arrange
        solicitudValida.setMonto(BigDecimal.ZERO);

        // Act
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_MONTO_O_PLAZO_INVALIDO))
                .verify();
    }
}
