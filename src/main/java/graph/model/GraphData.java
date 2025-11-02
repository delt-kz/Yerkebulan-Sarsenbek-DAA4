package graph.model;

import java.util.List;

/**
 * Representation of a dataset loaded from JSON.
 */
public record GraphData(WeightedDirectedGraph graph, int source, String weightModel, List<WeightedEdge> edges) {
}
