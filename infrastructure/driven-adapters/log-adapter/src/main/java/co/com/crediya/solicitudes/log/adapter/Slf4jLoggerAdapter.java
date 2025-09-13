package co.com.crediya.solicitudes.log.adapter;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter for the domain LoggerGateway using SLF4J.
 */
@Component
public class Slf4jLoggerAdapter implements LoggerGateway {

    private static final Logger log = LoggerFactory.getLogger(Slf4jLoggerAdapter.class);

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log.error(message, args);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        String formatted = MessageFormatter.arrayFormat(message, args).getMessage();
        log.error(formatted, throwable);
    }
}
