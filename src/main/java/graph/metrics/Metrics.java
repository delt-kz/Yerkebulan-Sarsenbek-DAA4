package graph.metrics;

import java.util.Map;

/**
 * Metrics collection interface that combines counters and timings.
 */
public interface Metrics {
    void incrementCounter(String name);

    void addToCounter(String name, long delta);

    long getCounter(String name);

    Map<String, Long> counters();

    void addTime(String name, long durationNanos);

    long getTime(String name);

    Map<String, Long> times();

    TimerContext time(String name);

    interface TimerContext extends AutoCloseable {
        @Override
        void close();
    }
}
