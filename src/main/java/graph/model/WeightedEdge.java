package graph.model;


public record WeightedEdge(int from, int to, double weight) {
    @Override
    public String toString() {
        return "(" + from + " -> " + to + ", w=" + weight + ")";
    }
}
