package graph.topo;

import graph.metrics.Metrics;
import graph.model.WeightedDirectedGraph;
import graph.model.WeightedEdge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class TopologicalSorter {
    public static final String COUNTER_QUEUE_PUSH = "topo.queuePush";
    public static final String COUNTER_QUEUE_POP = "topo.queuePop";
    public static final String TIMER_TOPO_SORT = "topo.totalTime";

    private TopologicalSorter() {
    }

    public static List<Integer> sort(WeightedDirectedGraph dag, Metrics metrics) {
        try (Metrics.TimerContext ignored = metrics.time(TIMER_TOPO_SORT)) {
            int n = dag.vertexCount();
            int[] indegree = new int[n];
            for (int v = 0; v < n; v++) {
                for (WeightedEdge edge : dag.edgesFrom(v)) {
                    indegree[edge.to()]++;
                }
            }

            Deque<Integer> queue = new ArrayDeque<>();
            for (int v = 0; v < n; v++) {
                if (indegree[v] == 0) {
                    queue.add(v);
                    metrics.incrementCounter(COUNTER_QUEUE_PUSH);
                }
            }

            List<Integer> order = new ArrayList<>(n);
            while (!queue.isEmpty()) {
                int v = queue.remove();
                metrics.incrementCounter(COUNTER_QUEUE_POP);
                order.add(v);
                for (WeightedEdge edge : dag.edgesFrom(v)) {
                    int to = edge.to();
                    indegree[to]--;
                    if (indegree[to] == 0) {
                        queue.add(to);
                        metrics.incrementCounter(COUNTER_QUEUE_PUSH);
                    }
                }
            }

            if (order.size() != n) {
                throw new IllegalStateException("Graph contains a cycle");
            }
            return order;
        }
    }

    public static List<Integer> expandOrder(List<Integer> componentOrder, List<List<Integer>> components) {
        List<Integer> order = new ArrayList<>();
        for (int componentIndex : componentOrder) {
            order.addAll(components.get(componentIndex));
        }
        return order;
    }
}
