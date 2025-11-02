package graph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class WeightedDirectedGraph {
    private final int vertexCount;
    private final List<List<WeightedEdge>> adjacency;

    public WeightedDirectedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount must be non-negative");
        }
        this.vertexCount = vertexCount;
        this.adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public void addEdge(int from, int to, double weight) {
        checkVertex(from);
        checkVertex(to);
        adjacency.get(from).add(new WeightedEdge(from, to, weight));
    }

    public List<WeightedEdge> edgesFrom(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public List<List<WeightedEdge>> adjacency() {
        return Collections.unmodifiableList(adjacency);
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= vertexCount) {
            throw new IndexOutOfBoundsException("Vertex " + v + " is out of bounds");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedDirectedGraph that = (WeightedDirectedGraph) o;
        return vertexCount == that.vertexCount && Objects.equals(adjacency, that.adjacency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexCount, adjacency);
    }
}
