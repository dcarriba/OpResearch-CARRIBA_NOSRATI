package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.MinimumCostFlow;
import com.dcarriba.model.graph.ResidualArc;
import com.dcarriba.model.graph.ResidualGraph;
import com.dcarriba.model.graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class MinimumCostFlowAlgorithm {
    private static final long INFINITY = Long.MAX_VALUE / 4;

    public MinimumCostFlow solveWithPathAlgorithm(Graph graph) {
        return solveWithPathAlgorithm(graph, Long.MAX_VALUE);
    }

    public MinimumCostFlow solveWithPathAlgorithm(Graph graph, ResidualGraphObserver residualGraphObserver) {
        return solveWithPathAlgorithm(graph, Long.MAX_VALUE, residualGraphObserver);
    }

    public MinimumCostFlow solveWithPathAlgorithm(Graph graph, long requestedFlow) {
        return solveWithPathAlgorithm(graph, requestedFlow, ResidualGraphObserver.NONE);
    }

    public MinimumCostFlow solveWithPathAlgorithm(
        Graph graph,
        long requestedFlow,
        ResidualGraphObserver residualGraphObserver
    ) {
        return solve(graph, requestedFlow, PathStrategy.BELLMAN_FORD, residualGraphObserver);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(Graph graph) {
        return solveWithDijkstraAndCostNormalization(graph, Long.MAX_VALUE);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(
        Graph graph,
        ResidualGraphObserver residualGraphObserver
    ) {
        return solveWithDijkstraAndCostNormalization(graph, Long.MAX_VALUE, residualGraphObserver);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(Graph graph, long requestedFlow) {
        return solveWithDijkstraAndCostNormalization(graph, requestedFlow, ResidualGraphObserver.NONE);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(
        Graph graph,
        long requestedFlow,
        ResidualGraphObserver residualGraphObserver
    ) {
        return solve(graph, requestedFlow, PathStrategy.DIJKSTRA_WITH_POTENTIALS, residualGraphObserver);
    }

    private MinimumCostFlow solve(
        Graph graph,
        long requestedFlow,
        PathStrategy pathStrategy,
        ResidualGraphObserver residualGraphObserver
    ) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(residualGraphObserver, "residualGraphObserver must not be null");
        if (requestedFlow < 0) {
            throw new IllegalArgumentException("Requested flow must be >= 0.");
        }

        Vertex source = Objects.requireNonNull(graph.getSource(), "graph source must not be null");
        Vertex sink = Objects.requireNonNull(graph.getSink(), "graph sink must not be null");
        if (source.equals(sink)) {
            throw new IllegalArgumentException("Source and sink must be different vertices.");
        }

        ResidualGraph residualGraph = new ResidualGraph(graph);
        Map<Arc, Long> flows = initializeFlows(graph);
        Map<Vertex, Long> potentials = pathStrategy == PathStrategy.DIJKSTRA_WITH_POTENTIALS
            ? initialPotentials(graph, residualGraph, source)
            : new HashMap<>();

        long sentFlow = 0;
        long minimumCost = 0;
        int step = 0;
        residualGraphObserver.onResidualGraphStep(pathStrategy.algorithmName(), step, residualGraph);

        while (sentFlow < requestedFlow) {
            List<ResidualArc> path = switch (pathStrategy) {
                case BELLMAN_FORD -> findShortestPathWithBellmanFord(
                        graph,
                        residualGraph,
                        source,
                        sink
                );
                case DIJKSTRA_WITH_POTENTIALS -> findShortestPathWithDijkstra(
                    residualGraph,
                    source,
                    sink,
                    potentials
                );
            };

            if (path.isEmpty()) {
                if (requestedFlow == Long.MAX_VALUE) {
                    break;
                }
                throw new IllegalArgumentException("Cannot send requested flow of " + requestedFlow + ".");
            }

            long bottleneck = Math.min(
                requestedFlow - sentFlow,
                path.stream().mapToLong(ResidualArc::getResidualCapacity).min().orElseThrow()
            );

            for (ResidualArc arc : path) {
                arc.augment(bottleneck);
                minimumCost += bottleneck * arc.getCost();

                if (arc.getOriginalArc() != null) {
                    flows.merge(
                        arc.getOriginalArc(),
                        arc.isForward() ? bottleneck : -bottleneck,
                        Long::sum
                    );
                }
            }

            sentFlow += bottleneck;
            step++;
            residualGraphObserver.onResidualGraphStep(pathStrategy.algorithmName(), step, residualGraph);
        }

        return new MinimumCostFlow(source, sink, sentFlow, minimumCost, flows);
    }

    private Map<Arc, Long> initializeFlows(Graph graph) {
        Map<Arc, Long> flows = new IdentityHashMap<>();
        for (Arc arc : graph.getArcs()) {
            flows.put(arc, 0L);
        }
        return flows;
    }

    private List<ResidualArc> findShortestPathWithBellmanFord(
        Graph graph,
        ResidualGraph residualGraph,
        Vertex source,
        Vertex sink
    ) {
        ShortestPathTree shortestPathTree = bellmanFord(graph, residualGraph, source, true);
        if (!shortestPathTree.distances().containsKey(sink)) {
            return List.of();
        }
        return buildPath(source, sink, shortestPathTree.parents());
    }

    private Map<Vertex, Long> initialPotentials(Graph graph, ResidualGraph residualGraph, Vertex source) {
        Map<Vertex, Long> distances = bellmanFord(graph, residualGraph, source, true).distances();
        Map<Vertex, Long> potentials = new HashMap<>();
        for (Vertex v : graph.getVertices()) {
            potentials.put(v, distances.getOrDefault(v, INFINITY));
        }
        return potentials;
    }

    private Map<Vertex, Long> recomputePotentialsFromResidual(ResidualGraph residualGraph, Vertex source) {
        Map<Vertex, Long> distances = new HashMap<>();
        distances.put(source, 0L);

        int n = residualGraph.getVertices().size();
        for (int i = 1; i < n; i++) {
            boolean changed = false;
            for (Vertex u : residualGraph.getVertices()) {
                Long du = distances.get(u);
                if (du == null) {
                    continue;
                }

                for (ResidualArc arc : residualGraph.getArcsFrom(u)) {
                    if (arc.getResidualCapacity() <= 0) {
                        continue;
                    }

                    long cand = du + arc.getCost();
                    if (cand < distances.getOrDefault(arc.getTo(), INFINITY)) {
                        distances.put(arc.getTo(), cand);
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }

        Map<Vertex, Long> potentials = new HashMap<>();
        for (Vertex v : residualGraph.getVertices()) {
            potentials.put(v, distances.getOrDefault(v, INFINITY));
        }
        return potentials;
    }

    private ShortestPathTree bellmanFord(
        Graph graph,
        ResidualGraph residualGraph,
        Vertex source,
        boolean failOnNegativeCycle
    ) {
        Map<Vertex, Long> distances = new HashMap<>();
        Map<Vertex, ResidualArc> parents = new HashMap<>();
        distances.put(source, 0L);

        for (int i = 1; i < graph.getNumberOfVertices(); i++) {
            boolean changed = false;

            for (Vertex from : graph.getVertices()) {
                Long distanceFrom = distances.get(from);
                if (distanceFrom == null) {
                    continue;
                }

                for (ResidualArc arc : residualGraph.getArcsFrom(from)) {
                    if (arc.getResidualCapacity() <= 0) {
                        continue;
                    }

                    long candidate = distanceFrom + arc.getCost();
                    if (candidate < distances.getOrDefault(arc.getTo(), INFINITY)) {
                        distances.put(arc.getTo(), candidate);
                        parents.put(arc.getTo(), arc);
                        changed = true;
                    }
                }
            }

            if (!changed) {
                break;
            }
        }

        if (failOnNegativeCycle && hasReachableNegativeCycle(graph, residualGraph, distances)) {
            throw new IllegalArgumentException("Graph contains a reachable negative-cost cycle.");
        }

        return new ShortestPathTree(distances, parents);
    }

    private boolean hasReachableNegativeCycle(
        Graph graph,
        ResidualGraph residualGraph,
        Map<Vertex, Long> distances
    ) {
        for (Vertex from : graph.getVertices()) {
            Long distanceFrom = distances.get(from);
            if (distanceFrom == null) {
                continue;
            }

            for (ResidualArc arc : residualGraph.getArcsFrom(from)) {
                if (arc.getResidualCapacity() > 0
                    && distanceFrom + arc.getCost() < distances.getOrDefault(arc.getTo(), INFINITY)) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<ResidualArc> findShortestPathWithDijkstra(
        ResidualGraph residualGraph,
        Vertex source,
        Vertex sink,
        Map<Vertex, Long> potentials
    ) {
        Map<Vertex, Long> distances = new HashMap<>();
        Map<Vertex, ResidualArc> parents = new HashMap<>();
        PriorityQueue<VertexDistance> queue = new PriorityQueue<>(Comparator.comparingLong(VertexDistance::distance));

        distances.put(source, 0L);
        queue.add(new VertexDistance(source, 0L));

        while (!queue.isEmpty()) {
            VertexDistance current = queue.remove();
            if (current.distance() != distances.getOrDefault(current.vertex(), INFINITY)) {
                continue;
            }

            if (current.vertex().equals(sink)) {
                break;
            }

            for (ResidualArc arc : residualGraph.getArcsFrom(current.vertex())) {
                if (arc.getResidualCapacity() <= 0) {
                    continue;
                }

                long potFrom = potentials.getOrDefault(arc.getFrom(), INFINITY);
                long potTo = potentials.getOrDefault(arc.getTo(), INFINITY);
                long reducedCost = arc.getCost() + potFrom - potTo;
                if (reducedCost < 0) {
                    String msg = String.format(
                        "Reduced cost must be non-negative. arc=%d->%d cost=%d potFrom=%d potTo=%d reduced=%d",
                        arc.getFrom().getId(), arc.getTo().getId(), arc.getCost(), potFrom, potTo, reducedCost
                    );
                    throw new IllegalStateException(msg);
                }

                long candidate = current.distance() + reducedCost;
                if (candidate < distances.getOrDefault(arc.getTo(), INFINITY)) {
                    distances.put(arc.getTo(), candidate);
                    parents.put(arc.getTo(), arc);
                    queue.add(new VertexDistance(arc.getTo(), candidate));
                }
            }
        }

        for (Map.Entry<Vertex, Long> entry : distances.entrySet()) {
            potentials.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        // Invariant check: all residual arcs must have non-negative reduced cost.
        boolean invariantOk = true;
        for (ResidualArc arc : residualGraph.getArcs()) {
            if (arc.getResidualCapacity() <= 0) {
                continue;
            }
            long potFrom = potentials.getOrDefault(arc.getFrom(), INFINITY);
            long potTo = potentials.getOrDefault(arc.getTo(), INFINITY);
            long reduced = arc.getCost() + potFrom - potTo;
            if (reduced < 0) {
                invariantOk = false;
                break;
            }
        }

        if (!invariantOk) {
            // Fallback: recompute potentials using Bellman-Ford on the residual graph
            Map<Vertex, Long> recomputed = recomputePotentialsFromResidual(residualGraph, source);
            for (Vertex v : recomputed.keySet()) {
                potentials.put(v, recomputed.getOrDefault(v, INFINITY));
            }
        }

        if (!parents.containsKey(sink)) {
            return List.of();
        }

        return buildPath(source, sink, parents);
    }

    private List<ResidualArc> buildPath(Vertex source, Vertex sink, Map<Vertex, ResidualArc> parents) {
        List<ResidualArc> path = new ArrayList<>();
        Vertex current = sink;

        while (!current.equals(source)) {
            ResidualArc edge = parents.get(current);
            if (edge == null) {
                return List.of();
            }
            path.add(edge);
            current = edge.getFrom();
        }

        Collections.reverse(path);
        return path;
    }

    private enum PathStrategy {
        BELLMAN_FORD("minimumCostFlowPathAlgorithm"),
        DIJKSTRA_WITH_POTENTIALS("minimumCostFlowDijkstraAndCostNormalization");

        private final String algorithmName;

        PathStrategy(String algorithmName) {
            this.algorithmName = algorithmName;
        }

        private String algorithmName() {
            return algorithmName;
        }
    }

    private record ShortestPathTree(Map<Vertex, Long> distances, Map<Vertex, ResidualArc> parents) {
    }

    private record VertexDistance(Vertex vertex, long distance) {
    }
}
