package co.com.crediya.solicitudes.consumer;

import co.com.crediya.solicitudes.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;

class UserRestConsumerAdapterTest {

    private static MockWebServer mockBackEnd;
    private static UserRestConsumerAdapter adapter;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        String baseUrl = mockBackEnd.url("/").toString();
        adapter = new UserRestConsumerAdapter(WebClient.builder(), baseUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("findByEmail returns User on 200 OK with valid JSON body")
    void findByEmail_success() {
        // Arrange
        String jsonBody = "{" +
                "\"name\":\"Juan\"," +
                "\"lastname\":\"Pérez\"," +
                "\"salary\":1234.56" +
                "}";
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(jsonBody));

        // Act
        Mono<User> result = adapter.findByEmail("juan.perez@example.com");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(u ->
                        u.getName().equals("Juan") &&
                        u.getLastname().equals("Pérez") &&
                        new BigDecimal("1234.56").compareTo(u.getSalary()) == 0
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("findByEmail returns empty Mono when server responds with error")
    void findByEmail_errorReturnsEmpty() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Act
        Mono<User> result = adapter.findByEmail("missing@example.com");

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }
}
