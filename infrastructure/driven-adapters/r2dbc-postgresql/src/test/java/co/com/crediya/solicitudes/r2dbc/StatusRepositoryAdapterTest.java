package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.StatusData;
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
class StatusRepositoryAdapterTest {

    @Mock
    private StatusDataRepository statusDataRepository;

    @InjectMocks
    private StatusRepositoryAdapter adapter;

    private StatusData buildEstadoData(Long id, String nombre, String descripcion) {
        StatusData data = new StatusData();
        data.setId(id);
        data.setName(nombre);
        data.setDescription(descripcion);
        return data;
    }

    @Test
    void findById_shouldMapAndReturnEstado_whenFound() {
        StatusData data = buildEstadoData(1L, "APROBADO", "Request aprobada");
        given(statusDataRepository.findById(1L)).willReturn(Mono.just(data));

        StepVerifier.create(adapter.findById(1L))
                .assertNext(estado -> {
                    assertThat(estado.getId()).isEqualTo(1L);
                    assertThat(estado.getName()).isEqualTo("APROBADO");
                    assertThat(estado.getDescription()).isEqualTo("Request aprobada");
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldCompleteEmpty_whenNotFound() {
        given(statusDataRepository.findById(999L)).willReturn(Mono.empty());

        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldMapAllEstados() {
        StatusData d1 = buildEstadoData(1L, "APROBADO", "Request aprobada");
        StatusData d2 = buildEstadoData(2L, "RECHAZADO", "Request rechazada");
        given(statusDataRepository.findAll()).willReturn(Flux.just(d1, d2));

        StepVerifier.create(adapter.findAll())
                .assertNext(e -> {
                    assertThat(e.getId()).isEqualTo(1L);
                    assertThat(e.getName()).isEqualTo("APROBADO");
                    assertThat(e.getDescription()).isEqualTo("Request aprobada");
                })
                .assertNext(e -> {
                    assertThat(e.getId()).isEqualTo(2L);
                    assertThat(e.getName()).isEqualTo("RECHAZADO");
                    assertThat(e.getDescription()).isEqualTo("Request rechazada");
                })
                .verifyComplete();
    }

    @Test
    void findAll_shouldCompleteEmpty_whenNoData() {
        given(statusDataRepository.findAll()).willReturn(Flux.empty());

        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findByNombre_shouldMapAndReturnEstado_whenFound() {
        StatusData data = buildEstadoData(3L, "PENDIENTE", "Request pendiente");
        given(statusDataRepository.findByName("PENDIENTE")).willReturn(Mono.just(data));

        StepVerifier.create(adapter.findByName("PENDIENTE"))
                .assertNext(estado -> {
                    assertThat(estado.getId()).isEqualTo(3L);
                    assertThat(estado.getName()).isEqualTo("PENDIENTE");
                    assertThat(estado.getDescription()).isEqualTo("Request pendiente");
                })
                .verifyComplete();
    }

    @Test
    void findByNombre_shouldCompleteEmpty_whenNotFound() {
        given(statusDataRepository.findByName(any())).willReturn(Mono.empty());

        StepVerifier.create(adapter.findByName("DESCONOCIDO"))
                .verifyComplete();
    }

    @Test
    void methods_shouldPropagateErrors() {
        RuntimeException ex = new RuntimeException("DB error");
        given(statusDataRepository.findById(1L)).willReturn(Mono.error(ex));
        given(statusDataRepository.findAll()).willReturn(Flux.error(ex));
        given(statusDataRepository.findByName("X")).willReturn(Mono.error(ex));

        StepVerifier.create(adapter.findById(1L))
                .expectErrorMatches(t -> t == ex)
                .verify();

        StepVerifier.create(adapter.findAll())
                .expectErrorMatches(t -> t == ex)
                .verify();

        StepVerifier.create(adapter.findByName("X"))
                .expectErrorMatches(t -> t == ex)
                .verify();
    }
}
