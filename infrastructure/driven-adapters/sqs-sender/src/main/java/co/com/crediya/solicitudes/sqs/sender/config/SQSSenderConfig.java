package co.com.crediya.solicitudes.sqs.sender.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@EnableConfigurationProperties(SQSSenderProperties.class)
@Configuration
public class SQSSenderConfig {

    @Bean
    public SqsAsyncClient configSqs(SQSSenderProperties properties) {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
        );

        return SqsAsyncClient.builder()
                .endpointOverride(resolveEndpoint(properties))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private URI resolveEndpoint(SQSSenderProperties properties) {
        if (properties.endpoint() != null) {
            return URI.create(properties.endpoint());
        }
        return null;
    }
}
