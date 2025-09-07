package co.com.crediya.solicitudes.config;

import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.solicitudes.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCasesConfigTest {

    @Test
    void debeConstruirYUsarSolicitudUseCaseDesdeUseCasesConfig() {
        // Arrange
        SolicitudRepository solicitudRepository = org.mockito.Mockito.mock(SolicitudRepository.class);
        TipoPrestamoRepository tipoPrestamoRepository = org.mockito.Mockito.mock(TipoPrestamoRepository.class);
        UseCasesConfig config = new UseCasesConfig();

        // Act: crear instancia mediante el método @Bean del config
        SolicitudUseCase useCase = config.solicitudUseCase(solicitudRepository, tipoPrestamoRepository);

        // Assert: instancia creada
        assertNotNull(useCase, "No se pudo construir SolicitudUseCase desde UseCasesConfig");

        // Además: invocar un método público para aportar cobertura real de la clase
        Solicitud solicitudInvalida = Solicitud.builder()
                .email("correo-invalido") // formato inválido dispara validación
                .build();

        StepVerifier.create(useCase.crearSolicitud(solicitudInvalida, "correo-invalido"))
                .expectError() // Cualquier error de validación es suficiente para ejecutar el flujo
                .verify();
    }
}