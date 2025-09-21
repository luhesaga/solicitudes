package co.com.crediya.solicitudes.sqs.sender;

import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SQSSenderTest {

    private SqsAsyncClient sqsAsyncClient;
    private ObjectMapper objectMapper;
    private SQSSenderProperties properties;

    private SQSSender sender;

    @BeforeEach
    void setUp() {
        sqsAsyncClient = mock(SqsAsyncClient.class);
        objectMapper = mock(ObjectMapper.class);
        properties = new SQSSenderProperties(
                "us-east-1",
                "https://sqs.us-east-1.amazonaws.com/123456789012/my-queue",
                null,
                "dummyAccessKey",
                "dummySecretKey"
        );

        sender = new SQSSender(properties, sqsAsyncClient, objectMapper);
    }

    @Test
    void shouldSendMessageToSqsSuccessfully() throws Exception {
        // Arrange
        Request request = sampleRequest(1001L);
        String payload = "{\"id\":1001}";
        when(objectMapper.writeValueAsString(request)).thenReturn(payload);

        SendMessageResponse response = SendMessageResponse.builder().messageId("mid-123").build();
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // Act
        Mono<Void> result = sender.send(request);

        // Assert
        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsAsyncClient, times(1)).sendMessage(captor.capture());
        SendMessageRequest smr = captor.getValue();
        assertThat(smr.queueUrl()).isEqualTo(properties.queueUrl());
        assertThat(smr.messageBody()).isEqualTo(payload);
        verify(objectMapper, times(1)).writeValueAsString(request);
    }

    @Test
    void shouldErrorWhenSerializationFails() throws Exception {
        // Arrange
        Request request = sampleRequest(2002L);
        when(objectMapper.writeValueAsString(request)).thenThrow(new JsonProcessingException("boom!"){});

        // Act
        Mono<Void> result = sender.send(request);

        // Assert
        StepVerifier.create(result).expectError(JsonProcessingException.class).verify();
        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldErrorWhenSqsSendFails() throws Exception {
        // Arrange
        Request request = sampleRequest(3003L);
        String payload = "{\"id\":3003}";
        when(objectMapper.writeValueAsString(request)).thenReturn(payload);

        CompletableFuture<SendMessageResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("SQS down"));
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(failed);

        // Act
        Mono<Void> result = sender.send(request);

        // Assert
        StepVerifier.create(result).expectError(RuntimeException.class).verify();
        verify(sqsAsyncClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    private Request sampleRequest(Long id) {
        return Request.builder()
                .id(id)
                .amount(new BigDecimal("1000"))
                .term(12)
                .email("user@example.com")
                .creationDate(LocalDateTime.now())
                .statusId(1L)
                .loanTypeId(10L)
                .loanTypeName("Personal")
                .interestRate(1.5)
                .statusName("CREATED")
                .userName("john")
                .salary(new BigDecimal("2000"))
                .approximateMonthlyAmount(new BigDecimal("100"))
                .build();
    }
}
