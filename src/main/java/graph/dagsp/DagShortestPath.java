package graph.dagsp;

import graph.metrics.Metrics;
import graph.model.WeightedDirectedGraph;
import graph.model.WeightedEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class DagShortestPath {
    public static final String COUNTER_RELAXATIONS = "dag.relaxations";
    public static final String TIMER_SHORTEST = "dag.shortestTime";
    public static final String TIMER_LONGEST = "dag.longestTime";

    private DagShortestPath() {
    }

    public static ShortestPathResult shortestPaths(WeightedDirectedGraph dag,
                                                   int source,
                                                   List<Integer> topologicalOrder,
                                                   Metrics metrics) {
        int n = dag.vertexCount();
        double[] distance = new double[n];
        int[] predecessor = new int[n];
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        Arrays.fill(predecessor, -1);
        distance[source] = 0.0;

        try (Metrics.TimerContext ignored = metrics.time(TIMER_SHORTEST)) {
            for (int vertex : topologicalOrder) {
                if (distance[vertex] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (WeightedEdge edge : dag.edgesFrom(vertex)) {
                    double candidate = distance[vertex] + edge.weight();
                    if (candidate < distance[edge.to()]) {
                        distance[edge.to()] = candidate;
                        predecessor[edge.to()] = vertex;
                        metrics.incrementCounter(COUNTER_RELAXATIONS);
                    }
                }
            }
        }

        return new ShortestPathResult(distance, predecessor, source);
    }

    public static CriticalPathResult longestPath(WeightedDirectedGraph dag,
                                                 List<Integer> topologicalOrder,
                                                 Metrics metrics) {
        int n = dag.vertexCount();
        double[] distance = new double[n];
        int[] predecessor = new int[n];
        Arrays.fill(distance, Double.NEGATIVE_INFINITY);
        Arrays.fill(predecessor, -1);

        int[] indegree = new int[n];
        for (int v = 0; v < n; v++) {
            for (WeightedEdge edge : dag.edgesFrom(v)) {
                indegree[edge.to()]++;
            }
        }
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                distance[v] = 0.0;
            }
        }

        try (Metrics.TimerContext ignored = metrics.time(TIMER_LONGEST)) {
            for (int vertex : topologicalOrder) {
                if (distance[vertex] == Double.NEGATIVE_INFINITY) {
                    continue;
                }
                for (WeightedEdge edge : dag.edgesFrom(vertex)) {
                    double candidate = distance[vertex] + edge.weight();
                    if (candidate > distance[edge.to()]) {
                        distance[edge.to()] = candidate;
                        predecessor[edge.to()] = vertex;
                        metrics.incrementCounter(COUNTER_RELAXATIONS);
                    }
                }
            }
        }

        double bestDistance = Double.NEGATIVE_INFINITY;
        int bestVertex = -1;
        for (int v = 0; v < n; v++) {
            if (distance[v] > bestDistance) {
                bestDistance = distance[v];
                bestVertex = v;
            }
        }

        List<Integer> path = bestVertex == -1 ? List.of() : reconstructPath(bestVertex, predecessor);
        return new CriticalPathResult(bestDistance, path);
    }

    private static List<Integer> reconstructPath(int target, int[] predecessor) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        while (current != -1) {
            path.add(current);
            current = predecessor[current];
        }
        Collections.reverse(path);
        return path;
    }

    public record ShortestPathResult(double[] distances, int[] predecessors, int source) {
        public List<Integer> buildPath(int target) {
            if (distances[target] == Double.POSITIVE_INFINITY) {
                return List.of();
            }
            List<Integer> path = new ArrayList<>();
            int current = target;
            while (current != -1) {
                path.add(current);
                if (current == source) {
                    break;
                }
                current = predecessors[current];
            }
            Collections.reverse(path);
            return path;
        }
    }

    public record CriticalPathResult(double length, List<Integer> path) {
    }
}
