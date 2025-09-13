package co.com.crediya.solicitudes.model.user.gateways;

import co.com.crediya.solicitudes.model.user.User;
import reactor.core.publisher.Mono;

public interface UserGateway {
    // Este es el contrato que nuestro UseCase necesita
    Mono<User> findByEmail(String email);
}
