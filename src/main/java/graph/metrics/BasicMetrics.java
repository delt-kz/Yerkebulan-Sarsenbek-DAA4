package graph.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BasicMetrics implements Metrics {
    private final Map<String, Long> counters = new HashMap<>();
    private final Map<String, Long> times = new HashMap<>();

    @Override
    public synchronized void incrementCounter(String name) {
        addToCounter(name, 1);
    }

    @Override
    public synchronized void addToCounter(String name, long delta) {
        counters.merge(name, delta, Long::sum);
    }

    @Override
    public synchronized long getCounter(String name) {
        return counters.getOrDefault(name, 0L);
    }

    @Override
    public synchronized Map<String, Long> counters() {
        return Collections.unmodifiableMap(new HashMap<>(counters));
    }

    @Override
    public synchronized void addTime(String name, long durationNanos) {
        times.merge(name, durationNanos, Long::sum);
    }

    @Override
    public synchronized long getTime(String name) {
        return times.getOrDefault(name, 0L);
    }

    @Override
    public synchronized Map<String, Long> times() {
        return Collections.unmodifiableMap(new HashMap<>(times));
    }

    @Override
    public TimerContext time(String name) {
        long start = System.nanoTime();
        return () -> addTime(name, System.nanoTime() - start);
    }
}
