package co.com.crediya.solicitudes.sqs.sender;

import co.com.crediya.solicitudes.model.notification.gateways.NotificationGateway;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> send(Request request) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(request))
                .flatMap(messageBody -> {
                    SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                            .queueUrl(properties.queueUrl()) // Obtenemos la URL de las propiedades
                            .messageBody(messageBody)
                            .build();

                    log.info("Enviando mensaje a SQS para solicitud ID: {}", request.getId());
                    return Mono.fromFuture(sqsAsyncClient.sendMessage(sendMessageRequest));
                })
                .doOnSuccess(response -> log.info("Mensaje enviado a SQS con ID: {}", response.messageId()))
                .doOnError(e -> log.error("Error al enviar mensaje a SQS", e))
                .then();
    }

}
