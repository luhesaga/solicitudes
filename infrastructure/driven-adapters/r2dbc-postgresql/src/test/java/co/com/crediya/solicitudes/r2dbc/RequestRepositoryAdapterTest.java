package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.r2dbc.data.RequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestRepositoryAdapterTest {

    private StatusDataRepository statusDataRepository;
    private ReactiveTransactionManager transactionManager;

    private RequestRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        statusDataRepository = Mockito.mock(StatusDataRepository.class);
        transactionManager = Mockito.mock(ReactiveTransactionManager.class);
        ReactiveTransaction tx = Mockito.mock(ReactiveTransaction.class);
        when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.just(tx));
        when(transactionManager.commit(any())).thenReturn(Mono.empty());
        when(transactionManager.rollback(any())).thenReturn(Mono.empty());

        adapter = new RequestRepositoryAdapter(statusDataRepository, transactionManager);
    }

    @Test
    void save_happyPath_savesAndMapsCorrectly() {
        Request request = Request.builder()
                .amount(new BigDecimal("12345.67"))
                .term(12)
                .email("test@example.com")
                .statusId(2L)
                .loanTypeId(5L)
                .build();

        RequestData savedData = new RequestData();
        savedData.setId(100L);
        savedData.setAmount(request.getAmount());
        savedData.setTerm(request.getTerm());
        savedData.setEmail(request.getEmail());
        savedData.setStatusId(request.getStatusId());
        savedData.setLoanTypeId(request.getLoanTypeId());

        when(statusDataRepository.save(any(RequestData.class))).thenReturn(Mono.just(savedData));

        Mono<Request> result = adapter.save(request);

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo(100L);
                    assertThat(saved.getAmount()).isEqualByComparingTo("12345.67");
                    assertThat(saved.getTerm()).isEqualTo(12);
                    assertThat(saved.getEmail()).isEqualTo("test@example.com");
                    assertThat(saved.getStatusId()).isEqualTo(2L);
                    assertThat(saved.getLoanTypeId()).isEqualTo(5L);
                })
                .verifyComplete();

        ArgumentCaptor<RequestData> captor = ArgumentCaptor.forClass(RequestData.class);
        verify(statusDataRepository, times(1)).save(captor.capture());
        RequestData dataSent = captor.getValue();
        assertThat(dataSent.getId()).isNull(); // not set before save
        assertThat(dataSent.getAmount()).isEqualByComparingTo("12345.67");
        assertThat(dataSent.getTerm()).isEqualTo(12);
        assertThat(dataSent.getEmail()).isEqualTo("test@example.com");
        assertThat(dataSent.getStatusId()).isEqualTo(2L);
        assertThat(dataSent.getLoanTypeId()).isEqualTo(5L);

        verify(transactionManager, times(1)).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void save_whenRepositoryErrors_propagatesAndRollsBack() {
        Request request = Request.builder()
                .amount(new BigDecimal("10"))
                .term(1)
                .email("fail@example.com")
                .statusId(1L)
                .loanTypeId(1L)
                .build();

        when(statusDataRepository.save(any(RequestData.class)))
                .thenReturn(Mono.error(new RuntimeException("db error")));

        StepVerifier.create(adapter.save(request))
                .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage().contains("db error"))
                .verify();

        verify(transactionManager, never()).commit(any());
        verify(transactionManager, times(1)).rollback(any());
    }

    @Test
    void findForManualReview_delegatesWithCorrectPagingAndMaps() {
        int page = 2; // 0-based
        int size = 3;
        int expectedOffset = page * size; // 6

        RequestData d1 = new RequestData();
        d1.setId(1L);
        d1.setAmount(new BigDecimal("100.00"));
        d1.setTerm(6);
        d1.setEmail("a@a.com");
        d1.setStatusId(1L);
        d1.setLoanTypeId(10L);

        RequestData d2 = new RequestData();
        d2.setId(2L);
        d2.setAmount(new BigDecimal("200.00"));
        d2.setTerm(12);
        d2.setEmail("b@b.com");
        d2.setStatusId(2L);
        d2.setLoanTypeId(20L);

        when(statusDataRepository.findForManualReview(size, expectedOffset))
                .thenReturn(Flux.just(d1, d2));

        StepVerifier.create(adapter.findForManualReview(page, size))
                .assertNext(s -> {
                    assertThat(s.getId()).isEqualTo(1L);
                    assertThat(s.getAmount()).isEqualByComparingTo("100.00");
                    assertThat(s.getTerm()).isEqualTo(6);
                    assertThat(s.getEmail()).isEqualTo("a@a.com");
                    assertThat(s.getStatusId()).isEqualTo(1L);
                    assertThat(s.getLoanTypeId()).isEqualTo(10L);
                })
                .assertNext(s -> {
                    assertThat(s.getId()).isEqualTo(2L);
                    assertThat(s.getAmount()).isEqualByComparingTo("200.00");
                    assertThat(s.getTerm()).isEqualTo(12);
                    assertThat(s.getEmail()).isEqualTo("b@b.com");
                    assertThat(s.getStatusId()).isEqualTo(2L);
                    assertThat(s.getLoanTypeId()).isEqualTo(20L);
                })
                .verifyComplete();

        verify(statusDataRepository, times(1)).findForManualReview(size, expectedOffset);
    }

    @Test
    void findForManualReview_whenEmpty_emitsComplete() {
        int page = 0;
        int size = 5;
        when(statusDataRepository.findForManualReview(size, 0)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findForManualReview(page, size))
                .verifyComplete();

        verify(statusDataRepository, times(1)).findForManualReview(size, 0);
    }
}
