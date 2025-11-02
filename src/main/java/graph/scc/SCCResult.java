package graph.scc;

import graph.model.WeightedDirectedGraph;

import java.util.List;

public record SCCResult(List<List<Integer>> components, int[] componentOf, WeightedDirectedGraph condensationGraph) {
}
