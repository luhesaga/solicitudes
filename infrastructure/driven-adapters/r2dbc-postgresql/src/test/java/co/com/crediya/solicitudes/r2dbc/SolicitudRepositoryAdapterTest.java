package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.r2dbc.data.SolicitudData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SolicitudRepositoryAdapterTest {

    private SolicitudDataRepository solicitudDataRepository;
    private ReactiveTransactionManager transactionManager;

    private SolicitudRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        solicitudDataRepository = Mockito.mock(SolicitudDataRepository.class);
        transactionManager = Mockito.mock(ReactiveTransactionManager.class);
        // Return a mocked ReactiveTransaction for any transaction request
        ReactiveTransaction tx = Mockito.mock(ReactiveTransaction.class);
        when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.just(tx));
        when(transactionManager.commit(any())).thenReturn(Mono.empty());
        when(transactionManager.rollback(any())).thenReturn(Mono.empty());

        adapter = new SolicitudRepositoryAdapter(solicitudDataRepository, transactionManager);
    }

    @Test
    void guardarSolicitud_happyPath_savesAndMapsCorrectly() {
        // Given a domain model
        Solicitud solicitud = Solicitud.builder()
                .monto(new BigDecimal("12345.67"))
                .plazo(12)
                .email("test@example.com")
                .idEstado(2L)
                .idTipoPrestamo(5L)
                .build();

        // And the repository returns a saved data entity (simulating DB assigned id)
        SolicitudData savedData = new SolicitudData();
        savedData.setIdSolicitud(100L);
        savedData.setMonto(solicitud.getMonto());
        savedData.setPlazo(solicitud.getPlazo());
        savedData.setEmail(solicitud.getEmail());
        savedData.setIdEstado(solicitud.getIdEstado());
        savedData.setIdTipoPrestamo(solicitud.getIdTipoPrestamo());

        when(solicitudDataRepository.save(any(SolicitudData.class))).thenReturn(Mono.just(savedData));

        // When
        Mono<Solicitud> result = adapter.guardarSolicitud(solicitud);

        // Then
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved.getIdSolicitud()).isEqualTo(100L);
                    assertThat(saved.getMonto()).isEqualByComparingTo("12345.67");
                    assertThat(saved.getPlazo()).isEqualTo(12);
                    assertThat(saved.getEmail()).isEqualTo("test@example.com");
                    assertThat(saved.getIdEstado()).isEqualTo(2L);
                    assertThat(saved.getIdTipoPrestamo()).isEqualTo(5L);
                })
                .verifyComplete();

        // And verify we mapped and called save with expected fields
        ArgumentCaptor<SolicitudData> captor = ArgumentCaptor.forClass(SolicitudData.class);
        verify(solicitudDataRepository, times(1)).save(captor.capture());
        SolicitudData dataSent = captor.getValue();
        assertThat(dataSent.getIdSolicitud()).isNull(); // not set before save
        assertThat(dataSent.getMonto()).isEqualByComparingTo("12345.67");
        assertThat(dataSent.getPlazo()).isEqualTo(12);
        assertThat(dataSent.getEmail()).isEqualTo("test@example.com");
        assertThat(dataSent.getIdEstado()).isEqualTo(2L);
        assertThat(dataSent.getIdTipoPrestamo()).isEqualTo(5L);

        // Ensure commit was attempted
        verify(transactionManager, times(1)).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void guardarSolicitud_whenRepositoryErrors_propagatesAndRollsBack() {
        // Given
        Solicitud solicitud = Solicitud.builder()
                .monto(new BigDecimal("10"))
                .plazo(1)
                .email("fail@example.com")
                .idEstado(1L)
                .idTipoPrestamo(1L)
                .build();

        when(solicitudDataRepository.save(any(SolicitudData.class)))
                .thenReturn(Mono.error(new RuntimeException("db error")));

        // When & Then
        StepVerifier.create(adapter.guardarSolicitud(solicitud))
                .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage().contains("db error"))
                .verify();

        // Verify rollback was attempted and no commit
        verify(transactionManager, never()).commit(any());
        verify(transactionManager, times(1)).rollback(any());
    }
}
