package graph;

import graph.dagsp.DagShortestPath;
import graph.metrics.BasicMetrics;
import graph.model.WeightedDirectedGraph;
import graph.topo.TopologicalSorter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {
    @Test
    void computesShortestAndLongestPaths() {
        WeightedDirectedGraph dag = new WeightedDirectedGraph(4);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 2);
        dag.addEdge(1, 3, 3);
        dag.addEdge(2, 3, 1);

        List<Integer> order = TopologicalSorter.sort(dag, new BasicMetrics());
        DagShortestPath.ShortestPathResult shortest = DagShortestPath.shortestPaths(dag, 0, order, new BasicMetrics());
        assertEquals(0.0, shortest.distances()[0]);
        assertEquals(1.0, shortest.distances()[1]);
        assertEquals(2.0, shortest.distances()[2]);
        assertEquals(3.0, shortest.distances()[3]);
        assertEquals(List.of(0, 2, 3), shortest.buildPath(3));

        DagShortestPath.CriticalPathResult longest = DagShortestPath.longestPath(dag, order, new BasicMetrics());
        assertEquals(4.0, longest.length());
        assertEquals(List.of(0, 1, 3), longest.path());
    }
}
