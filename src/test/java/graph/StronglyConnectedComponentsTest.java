package graph;

import graph.metrics.BasicMetrics;
import graph.model.WeightedDirectedGraph;
import graph.scc.SCCResult;
import graph.scc.StronglyConnectedComponents;
import graph.topo.TopologicalSorter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StronglyConnectedComponentsTest {
    @Test
    void detectsComponentsAndBuildsCondensation() {
        WeightedDirectedGraph graph = new WeightedDirectedGraph(5);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);

        SCCResult result = StronglyConnectedComponents.compute(graph, new BasicMetrics());

        assertEquals(3, result.components().size());
        assertEquals(3, result.condensationGraph().vertexCount());

        // Components should cover all vertices
        int totalVertices = result.components().stream().mapToInt(List::size).sum();
        assertEquals(5, totalVertices);

        List<Integer> topoOrder = TopologicalSorter.sort(result.condensationGraph(), new BasicMetrics());
        assertEquals(3, topoOrder.size());
    }
}
