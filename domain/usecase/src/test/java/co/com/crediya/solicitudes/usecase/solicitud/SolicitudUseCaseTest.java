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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudUseCaseTest { // Se cambió a 'class' en lugar de 'public class' por convención

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud solicitudValida;
    private TipoPrestamo tipoPrestamoValido;
    private String emailAutenticadoValido;

    @BeforeEach
    void setUp() {
        tipoPrestamoValido = TipoPrestamo.builder()
                .idTipoPrestamo(1L)
                .nombre("Libre Inversión")
                .montoMinimo(new BigDecimal("1000000.00"))
                .montoMaximo(new BigDecimal("50000000.00"))
                .build();

        emailAutenticadoValido = "test@example.com";

        solicitudValida = Solicitud.builder()
                .monto(new BigDecimal("5000000.00"))
                .plazo(24)
                .email(emailAutenticadoValido) // El email de la solicitud coincide con el autenticado
                .idTipoPrestamo(1L)
                .build();
    }

    @Test
    @DisplayName("Debería crear una solicitud exitosamente cuando todos los datos son válidos")
    void deberiaCrearSolicitudExitosamente() {
        // Arrange
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.just(tipoPrestamoValido));
        Solicitud solicitudGuardada = solicitudValida.toBuilder().idSolicitud(100L).idEstado(1L).build();
        when(solicitudRepository.guardarSolicitud(any(Solicitud.class))).thenReturn(Mono.just(solicitudGuardada));

        // Act
        // CAMBIO: Se pasa el email del usuario autenticado como segundo parámetro
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida, emailAutenticadoValido);

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(solicitud -> solicitud.getIdSolicitud() == 100L && solicitud.getIdEstado() == 1L)
                .verifyComplete();
    }

    // --- INICIO DE LA NUEVA PRUEBA DE SEGURIDAD ---
    @Test
    @DisplayName("Debería lanzar excepción si el email autenticado no coincide con el de la solicitud")
    void deberiaLanzarErrorSiEmailNoCoincide() {
        // Arrange
        String otroEmail = "atacante@example.com";

        // Act
        // CAMBIO: Se llama al método con el email de la solicitud y un email de autenticación DIFERENTE
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida, otroEmail);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().contains("No tiene permiso"))
                .verify();
    }
    // --- FIN DE LA NUEVA PRUEBA ---

    @Test
    @DisplayName("Debería lanzar excepción si el tipo de préstamo no existe")
    void deberiaLanzarErrorCuandoTipoPrestamoNoExiste() {
        // Arrange
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act
        // CAMBIO: Se pasa el email del usuario autenticado
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida, emailAutenticadoValido);

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
        solicitudValida.setMonto(new BigDecimal("500000.00"));
        when(tipoPrestamoRepository.findById(anyLong())).thenReturn(Mono.just(tipoPrestamoValido));

        // Act
        // CAMBIO: Se pasa el email del usuario autenticado
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida, emailAutenticadoValido);

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
        // CAMBIO: Se pasa el email del usuario autenticado
        Mono<Solicitud> resultado = solicitudUseCase.crearSolicitud(solicitudValida, emailAutenticadoValido);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable -> throwable instanceof BusinessValidationException &&
                        throwable.getMessage().equals(Constants.ERROR_MONTO_O_PLAZO_INVALIDO))
                .verify();
    }
}