package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.estado.Estado;
import co.com.crediya.solicitudes.r2dbc.data.EstadoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EstadoRepositoryAdapterTest {

    @Mock
    private EstadoDataRepository estadoDataRepository;

    @InjectMocks
    private EstadoRepositoryAdapter adapter;

    private EstadoData buildEstadoData(Long id, String nombre, String descripcion) {
        EstadoData data = new EstadoData();
        data.setIdEstado(id);
        data.setNombre(nombre);
        data.setDescripcion(descripcion);
        return data;
    }

    @Test
    void findById_shouldMapAndReturnEstado_whenFound() {
        // Arrange
        EstadoData data = buildEstadoData(1L, "APROBADO", "Solicitud aprobada");
        given(estadoDataRepository.findById(1L)).willReturn(Mono.just(data));

        // Act & Assert
        StepVerifier.create(adapter.findById(1L))
                .assertNext(estado -> {
                    assertThat(estado.getIdEstado()).isEqualTo(1L);
                    assertThat(estado.getNombre()).isEqualTo("APROBADO");
                    assertThat(estado.getDescripcion()).isEqualTo("Solicitud aprobada");
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldCompleteEmpty_whenNotFound() {
        // Arrange
        given(estadoDataRepository.findById(999L)).willReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldMapAllEstados() {
        // Arrange
        EstadoData d1 = buildEstadoData(1L, "APROBADO", "Solicitud aprobada");
        EstadoData d2 = buildEstadoData(2L, "RECHAZADO", "Solicitud rechazada");
        given(estadoDataRepository.findAll()).willReturn(Flux.just(d1, d2));

        // Act & Assert
        StepVerifier.create(adapter.findAll())
                .assertNext(e -> {
                    assertThat(e.getIdEstado()).isEqualTo(1L);
                    assertThat(e.getNombre()).isEqualTo("APROBADO");
                    assertThat(e.getDescripcion()).isEqualTo("Solicitud aprobada");
                })
                .assertNext(e -> {
                    assertThat(e.getIdEstado()).isEqualTo(2L);
                    assertThat(e.getNombre()).isEqualTo("RECHAZADO");
                    assertThat(e.getDescripcion()).isEqualTo("Solicitud rechazada");
                })
                .verifyComplete();
    }

    @Test
    void findAll_shouldCompleteEmpty_whenNoData() {
        // Arrange
        given(estadoDataRepository.findAll()).willReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findByNombre_shouldMapAndReturnEstado_whenFound() {
        // Arrange
        EstadoData data = buildEstadoData(3L, "PENDIENTE", "Solicitud pendiente");
        given(estadoDataRepository.findByNombre(eq("PENDIENTE"))).willReturn(Mono.just(data));

        // Act & Assert
        StepVerifier.create(adapter.findByNombre("PENDIENTE"))
                .assertNext(estado -> {
                    assertThat(estado.getIdEstado()).isEqualTo(3L);
                    assertThat(estado.getNombre()).isEqualTo("PENDIENTE");
                    assertThat(estado.getDescripcion()).isEqualTo("Solicitud pendiente");
                })
                .verifyComplete();
    }

    @Test
    void findByNombre_shouldCompleteEmpty_whenNotFound() {
        // Arrange
        given(estadoDataRepository.findByNombre(any())).willReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findByNombre("DESCONOCIDO"))
                .verifyComplete();
    }

    @Test
    void methods_shouldPropagateErrors() {
        // Arrange
        RuntimeException ex = new RuntimeException("DB error");
        given(estadoDataRepository.findById(1L)).willReturn(Mono.error(ex));
        given(estadoDataRepository.findAll()).willReturn(Flux.error(ex));
        given(estadoDataRepository.findByNombre("X")).willReturn(Mono.error(ex));

        // Act & Assert
        StepVerifier.create(adapter.findById(1L))
                .expectErrorMatches(t -> t == ex)
                .verify();

        StepVerifier.create(adapter.findAll())
                .expectErrorMatches(t -> t == ex)
                .verify();

        StepVerifier.create(adapter.findByNombre("X"))
                .expectErrorMatches(t -> t == ex)
                .verify();
    }
}
