package graph;

import graph.metrics.BasicMetrics;
import graph.model.WeightedDirectedGraph;
import graph.topo.TopologicalSorter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSorterTest {
    @Test
    void producesValidOrder() {
        WeightedDirectedGraph dag = new WeightedDirectedGraph(4);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);

        List<Integer> order = TopologicalSorter.sort(dag, new BasicMetrics());
        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }
}
