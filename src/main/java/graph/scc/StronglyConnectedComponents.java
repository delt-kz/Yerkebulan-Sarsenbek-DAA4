package graph.scc;

import graph.metrics.Metrics;
import graph.model.WeightedDirectedGraph;
import graph.model.WeightedEdge;

import java.util.*;


public class StronglyConnectedComponents {
    public static final String COUNTER_DFS_VISITS = "scc.dfsVisits";
    public static final String COUNTER_DFS_EDGES = "scc.dfsEdges";
    public static final String TIMER_SCC = "scc.totalTime";

    private int index;
    private final List<List<Integer>> components = new ArrayList<>();
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final boolean[] onStack;
    private final int[] indices;
    private final int[] lowLink;
    private final int[] componentOf;

    private final WeightedDirectedGraph graph;
    private final Metrics metrics;

    private StronglyConnectedComponents(WeightedDirectedGraph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
        int n = graph.vertexCount();
        this.onStack = new boolean[n];
        this.indices = new int[n];
        Arrays.fill(this.indices, -1);
        this.lowLink = new int[n];
        this.componentOf = new int[n];
        Arrays.fill(this.componentOf, -1);
    }

    public static SCCResult compute(WeightedDirectedGraph graph, Metrics metrics) {
        StronglyConnectedComponents tarjan = new StronglyConnectedComponents(graph, metrics);
        try (Metrics.TimerContext ignored = metrics.time(TIMER_SCC)) {
            for (int v = 0; v < graph.vertexCount(); v++) {
                if (tarjan.indices[v] == -1) {
                    tarjan.strongConnect(v);
                }
            }
        }
        WeightedDirectedGraph condensation = tarjan.buildCondensationGraph();
        return new SCCResult(List.copyOf(tarjan.components), tarjan.componentOf, condensation);
    }

    private void strongConnect(int v) {
        metrics.incrementCounter(COUNTER_DFS_VISITS);
        indices[v] = index;
        lowLink[v] = index;
        index++;
        stack.push(v);
        onStack[v] = true;

        for (WeightedEdge edge : graph.edgesFrom(v)) {
            metrics.incrementCounter(COUNTER_DFS_EDGES);
            int w = edge.to();
            if (indices[w] == -1) {
                strongConnect(w);
                lowLink[v] = Math.min(lowLink[v], lowLink[w]);
            } else if (onStack[w]) {
                lowLink[v] = Math.min(lowLink[v], indices[w]);
            }
        }

        if (lowLink[v] == indices[v]) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                componentOf[w] = components.size();
                component.add(w);
            } while (w != v);
            components.add(component);
        }
    }

    private WeightedDirectedGraph buildCondensationGraph() {
        WeightedDirectedGraph condensation = new WeightedDirectedGraph(components.size());
        for (int v = 0; v < graph.vertexCount(); v++) {
            int fromComponent = componentOf[v];
            for (WeightedEdge edge : graph.edgesFrom(v)) {
                int toComponent = componentOf[edge.to()];
                if (fromComponent != toComponent) {
                    condensation.addEdge(fromComponent, toComponent, edge.weight());
                }
            }
        }
        return condensation;
    }
}
