package graph.model;

import java.util.List;

public record GraphData(WeightedDirectedGraph graph, int source, String weightModel, List<WeightedEdge> edges) {
}
