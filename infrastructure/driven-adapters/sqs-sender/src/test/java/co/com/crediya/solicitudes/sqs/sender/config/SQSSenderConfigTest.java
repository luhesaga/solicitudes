package co.com.crediya.solicitudes.sqs.sender.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.junit.jupiter.api.Assertions.*;

class SQSSenderConfigTest {

    @Test
    void resolveEndpoint_returnsUri_whenEndpointProvided() throws Exception {
        SQSSenderConfig config = new SQSSenderConfig();
        SQSSenderProperties props = new SQSSenderProperties("us-east-1", "queue", "http://localhost:4566", "key", "secret");

        URI uri = invokeResolveEndpoint(config, props);

        assertNotNull(uri, "Endpoint URI should not be null when endpoint is provided");
        assertEquals("http://localhost:4566", uri.toString());
    }

    @Test
    void resolveEndpoint_returnsNull_whenEndpointIsNull() throws Exception {
        SQSSenderConfig config = new SQSSenderConfig();
        SQSSenderProperties props = new SQSSenderProperties("us-east-1", "queue", null, "key", "secret");

        URI uri = invokeResolveEndpoint(config, props);

        assertNull(uri, "Endpoint URI should be null when endpoint property is null");
    }

    @Test
    void configSqs_buildsClient_withGivenProperties() {
        SQSSenderConfig config = new SQSSenderConfig();
        SQSSenderProperties props = new SQSSenderProperties("us-east-1", "queue", "http://localhost:4566", "key", "secret");

        SqsAsyncClient client = config.configSqs(props);
        assertNotNull(client, "SqsAsyncClient should be created and not null");

        assertDoesNotThrow(client::close);
    }

    private URI invokeResolveEndpoint(SQSSenderConfig config, SQSSenderProperties props)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = SQSSenderConfig.class.getDeclaredMethod("resolveEndpoint", SQSSenderProperties.class);
        m.setAccessible(true);
        return (URI) m.invoke(config, props);
    }
}
