package co.com.crediya.solicitudes.metrics.aws;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricRecord;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MicrometerMetricPublisherTest {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @Test
    void publish_recordsTimerAndCounter_withTags() throws InterruptedException {
        // Arrange
        MetricCollection collection = mock(MetricCollection.class);

        MetricRecord tagString = mock(MetricRecord.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(tagString.value()).thenReturn("abc");
        when(tagString.metric().name()).thenReturn("tag.key");

        MetricRecord tagBool = mock(MetricRecord.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(tagBool.value()).thenReturn(true);
        when(tagBool.metric().name()).thenReturn("flag");

        MetricRecord durationRecord = mock(MetricRecord.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(durationRecord.value()).thenReturn(Duration.ofMillis(123));
        when(durationRecord.metric().name()).thenReturn("latency");

        MetricRecord counterRecord = mock(MetricRecord.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(counterRecord.value()).thenReturn(5);
        when(counterRecord.metric().name()).thenReturn("processed");

        MetricRecord ignoredRecord = mock(MetricRecord.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(ignoredRecord.value()).thenReturn(10L); // not Duration or Integer
        when(ignoredRecord.metric().name()).thenReturn("ignored");

        when(collection.stream()).thenAnswer(inv -> Stream.of(tagString, tagBool, durationRecord, counterRecord, ignoredRecord));

        MicrometerMetricPublisher publisher = new MicrometerMetricPublisher(registry);

        // Act
        publisher.publish(collection);
        // Await async execution (poll until timer is created or timeout)
        var deadline = System.currentTimeMillis() + 2000;
        while (registry.find("latency").tags("tag.key", "abc", "flag", "true").timer() == null
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }

        // Assert timer
        var timer = registry.find("latency").tags("tag.key", "abc", "flag", "true").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isBetween(120.0, 130.0);

        // Assert counter
        Counter counter = registry.find("processed").tags("tag.key", "abc", "flag", "true").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(5.0);

        // No meter for ignored type
        assertThat(registry.find("ignored").meter()).isNull();
    }
}