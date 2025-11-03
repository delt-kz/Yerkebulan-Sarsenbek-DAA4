package graph.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GraphLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GraphLoader() {
    }

    public static GraphData load(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            JsonNode root = OBJECT_MAPPER.readTree(stream);
            int n = root.get("n").asInt();
            WeightedDirectedGraph graph = new WeightedDirectedGraph(n);
            List<WeightedEdge> edges = new ArrayList<>();
            for (JsonNode edgeNode : root.withArray("edges")) {
                int u = edgeNode.get("u").asInt();
                int v = edgeNode.get("v").asInt();
                double w = edgeNode.path("w").asDouble(1.0);
                WeightedEdge edge = new WeightedEdge(u, v, w);
                graph.addEdge(u, v, w);
                edges.add(edge);
            }
            int source = root.path("source").asInt(0);
            String weightModel = root.path("weight_model").asText("edge");
            return new GraphData(graph, source, weightModel, List.copyOf(edges));
        }
    }
}
