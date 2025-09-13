package co.com.crediya.solicitudes.consumer.config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestConsumerConfigTest {

    @Test
    @DisplayName("WebClient built by RestConsumerConfig should apply baseUrl and default Content-Type header")
    void testWebClientConfiguration() {
        // Arrange
        String baseUrl = "http://example.com/api";
        int timeout = 1500;
        RestConsumerConfig config = new RestConsumerConfig(baseUrl, timeout);

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction captureExchange = request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient.Builder builder = WebClient.builder().exchangeFunction(captureExchange);

        // Act: build WebClient using config and execute a sample GET and POST
        WebClient webClient = config.getWebClient(builder);

        // Trigger a GET request and ensure it completes
        webClient.get().uri("/test-get").retrieve().toBodilessEntity().block();

        // Assert GET request
        ClientRequest getReq = capturedRequest.get();
        assertThat(getReq).as("Captured GET request").isNotNull();
        assertThat(getReq.url()).as("GET URI should include baseUrl").isEqualTo(URI.create(baseUrl + "/test-get"));
        assertThat(getReq.headers().getFirst(HttpHeaders.CONTENT_TYPE))
                .as("Default Content-Type header should be set")
                .isEqualTo("application/json");

        // Trigger a POST request and ensure it completes
        webClient.post().uri("/test-post").body(BodyInserters.fromValue("{}"))
                .retrieve().toBodilessEntity().block();

        // Assert POST request
        ClientRequest postReq = capturedRequest.get();
        assertThat(postReq).as("Captured POST request").isNotNull();
        assertThat(postReq.url()).as("POST URI should include baseUrl").isEqualTo(URI.create(baseUrl + "/test-post"));
        assertThat(postReq.headers().getFirst(HttpHeaders.CONTENT_TYPE))
                .as("Default Content-Type header should be set for POST as well")
                .isEqualTo("application/json");
    }

    @Test
    @DisplayName("WebClient should enforce read timeout configured in RestConsumerConfig")
    void testReadTimeoutIsEnforced() throws Exception {
        MockWebServer server = new MockWebServer();
        try {
            server.start();

            // Arrange: tiny timeout
            int timeoutMillis = 200;
            String baseUrl = server.url("/").toString();
            RestConsumerConfig config = new RestConsumerConfig(baseUrl, timeoutMillis);
            WebClient webClient = config.getWebClient(WebClient.builder());

            // Enqueue a response that delays the body longer than timeout to trigger ReadTimeout
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody("{\"ok\":true}")
                    .setBodyDelay(1, TimeUnit.SECONDS));

            // Act & Assert: the request should timeout
            assertThatThrownBy(() ->
                    webClient.get()
                            .uri("/slow")
                            .retrieve()
                            .toBodilessEntity()
                            .block(Duration.ofSeconds(5))
            )
            .as("Expect a timeout error caused by the configured ReadTimeoutHandler")
            .isInstanceOf(RuntimeException.class);
        } finally {
            server.shutdown();
        }
    }
}
