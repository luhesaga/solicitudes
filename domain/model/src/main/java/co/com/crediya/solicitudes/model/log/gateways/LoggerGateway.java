package co.com.crediya.solicitudes.model.log.gateways;

/**
 * Domain logging port to keep domain layer pure from logging frameworks.
 */
public interface LoggerGateway {

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    void error(String message, Throwable throwable, Object... args);
}
