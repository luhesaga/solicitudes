package co.com.crediya.solicitudes.r2dbc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlConnectionPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "spring.r2dbc.host=localhost",
                    "spring.r2dbc.port=5432",
                    "spring.r2dbc.database=mydb",
                    "spring.r2dbc.schema=public",
                    "spring.r2dbc.username=user",
                    "spring.r2dbc.password=secret"
            );

    @Test
    void shouldBindAllProperties() {
        contextRunner.run(context -> {
            PostgresqlConnectionProperties props = context.getBean(PostgresqlConnectionProperties.class);
            assertThat(props).isNotNull();
            assertThat(props.host()).isEqualTo("localhost");
            assertThat(props.port()).isEqualTo(5432);
            assertThat(props.database()).isEqualTo("mydb");
            assertThat(props.schema()).isEqualTo("public");
            assertThat(props.username()).isEqualTo("user");
            assertThat(props.password()).isEqualTo("secret");
        });
    }

    @Configuration
    @EnableConfigurationProperties(PostgresqlConnectionProperties.class)
    static class TestConfig { }
}
