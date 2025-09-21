package co.com.crediya.solicitudes.model.notification.gateways;

import co.com.crediya.solicitudes.model.request.Request;
import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> send(Request request);
}
