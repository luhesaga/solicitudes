package co.com.crediya.solicitudes.log.adapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class Slf4jLoggerAdapterTest {

    private Slf4jLoggerAdapter adapter;
    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        adapter = new Slf4jLoggerAdapter();
        logger = (Logger) LoggerFactory.getLogger(Slf4jLoggerAdapter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        if (logger != null && listAppender != null) {
            logger.detachAppender(listAppender);
            listAppender.stop();
        }
    }

    @Test
    void info_shouldLogFormattedMessage_withArguments() {
        adapter.info("Hello {}! Value={}.", "World", 42);

        assertEquals(1, listAppender.list.size(), "One log event expected");
        ILoggingEvent event = listAppender.list.get(0);
        assertEquals(Level.INFO, event.getLevel());
        assertEquals("Hello World! Value=42.", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    void warn_shouldLogMessage() {
        adapter.warn("Warning: {}", "Be careful");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertEquals(Level.WARN, event.getLevel());
        assertEquals("Warning: Be careful", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    void error_shouldLogMessage_withoutThrowable() {
        adapter.error("Error happened: {}", "boom");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Error happened: boom", event.getFormattedMessage());
        assertNull(event.getThrowableProxy());
    }

    @Test
    void error_withThrowable_shouldFormatMessage_andAttachThrowable() {
        RuntimeException ex = new RuntimeException("Kaboom");
        adapter.error("Failed {} at {}", ex, "process", 123);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        // error(message, throwable, args...) formats message first and logs with throwable
        assertEquals("Failed process at 123", event.getFormattedMessage());
        assertNotNull(event.getThrowableProxy());
        assertEquals(RuntimeException.class.getName(), event.getThrowableProxy().getClassName());
        assertEquals("Kaboom", event.getThrowableProxy().getMessage());
    }

    @Test
    void error_withThrowable_noArgs_shouldLogOriginalMessage() {
        IllegalStateException ex = new IllegalStateException("bad state");
        adapter.error("Just fails", ex);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Just fails", event.getFormattedMessage());
        assertNotNull(event.getThrowableProxy());
        assertEquals(IllegalStateException.class.getName(), event.getThrowableProxy().getClassName());
        assertEquals("bad state", event.getThrowableProxy().getMessage());
    }
}
