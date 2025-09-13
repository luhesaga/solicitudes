package co.com.crediya.solicitudes.consumer;

import co.com.crediya.solicitudes.model.user.User;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserRestConsumerAdapter implements UserGateway {

    private final WebClient webClient;

    public UserRestConsumerAdapter(WebClient.Builder webClientBuilder,
                                   @Value("${adapters.restconsumer.url.autenticacion}") String urlBase) {
        this.webClient = webClientBuilder.baseUrl(urlBase).build();
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return webClient.get()
                .uri("/api/v1/usuarios/email/{email}", email)
                .retrieve()
                .bodyToMono(User.class)
                .onErrorResume(e -> Mono.empty());
    }
}