package graph;

import graph.dagsp.DagShortestPath;
import graph.dagsp.DagShortestPath.CriticalPathResult;
import graph.dagsp.DagShortestPath.ShortestPathResult;
import graph.metrics.BasicMetrics;
import graph.metrics.Metrics;
import graph.model.GraphData;
import graph.model.GraphLoader;
import graph.model.WeightedDirectedGraph;
import graph.scc.SCCResult;
import graph.scc.StronglyConnectedComponents;
import graph.topo.TopologicalSorter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws IOException {
        Path dataDirectory = Path.of("data");
        if (!Files.isDirectory(dataDirectory)) {
            System.err.println("Data directory not found: " + dataDirectory.toAbsolutePath());
            return;
        }

        List<Path> datasets;
        try (Stream<Path> stream = Files.list(dataDirectory)) {
            datasets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .collect(Collectors.toList());
        }

        if (datasets.isEmpty()) {
            System.err.println("No datasets found in " + dataDirectory.toAbsolutePath());
            return;
        }

        for (Path datasetPath : datasets) {
            System.out.println("==================================================");
            System.out.println("Dataset: " + datasetPath.getFileName());
            System.out.println("Path: " + datasetPath.toAbsolutePath());
            System.out.println("==================================================");
            analyzeDataset(datasetPath);
            System.out.println();
        }
    }

    private static void analyzeDataset(Path datasetPath) throws IOException {
        GraphData data = GraphLoader.load(datasetPath);
        Metrics metrics = new BasicMetrics();

        System.out.println("Loaded graph with " + data.graph().vertexCount() + " vertices and " + data.edges().size() + " edges.");
        System.out.println("Source vertex for shortest paths: " + data.source());
        System.out.println("Weight model: " + data.weightModel());

        SCCResult sccResult = StronglyConnectedComponents.compute(data.graph(), metrics);
        System.out.println("\nStrongly connected components (" + sccResult.components().size() + "):");
        for (int i = 0; i < sccResult.components().size(); i++) {
            List<Integer> component = sccResult.components().get(i);
            System.out.println("Component " + i + " (size=" + component.size() + "): " + component);
        }

        WeightedDirectedGraph condensation = sccResult.condensationGraph();
        List<Integer> componentOrder = TopologicalSorter.sort(condensation, metrics);
        System.out.println("\nTopological order of components: " + componentOrder);
        System.out.println("Expanded task order: " + TopologicalSorter.expandOrder(componentOrder, sccResult.components()));

        ShortestPathResult shortest = DagShortestPath.shortestPaths(condensation, sccResult.componentOf()[data.source()], componentOrder, metrics);
        int lastComponent = componentOrder.get(componentOrder.size() - 1);
        System.out.println("\nShortest distances on condensation DAG from component " + sccResult.componentOf()[data.source()] + ":");
        double[] distances = shortest.distances();
        for (int c = 0; c < distances.length; c++) {
            System.out.printf("  to component %d = %s%n", c,
                    distances[c] == Double.POSITIVE_INFINITY ? "INF" : String.format("%.2f", distances[c]));
        }
        System.out.println("Example shortest path to last component: " + shortest.buildPath(lastComponent));

        CriticalPathResult criticalPath = DagShortestPath.longestPath(condensation, componentOrder, metrics);
        System.out.printf("\nCritical path length: %.2f%n", criticalPath.length());
        System.out.println("Critical path: " + criticalPath.path());

        System.out.println("\nMetrics summary:");
        metrics.counters().forEach((k, v) -> System.out.println("  " + k + " = " + v));
        metrics.times().forEach((k, v) -> System.out.println("  " + k + " = " + v + " ns"));
    }
}
