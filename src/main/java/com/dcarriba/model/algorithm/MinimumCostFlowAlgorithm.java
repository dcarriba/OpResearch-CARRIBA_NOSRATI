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

    public MinimumCostFlow solveWithPathAlgorithm(Graph graph, long requestedFlow) {
        return solve(graph, requestedFlow, PathStrategy.BELLMAN_FORD);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(Graph graph) {
        return solveWithDijkstraAndCostNormalization(graph, Long.MAX_VALUE);
    }

    public MinimumCostFlow solveWithDijkstraAndCostNormalization(Graph graph, long requestedFlow) {
        return solve(graph, requestedFlow, PathStrategy.DIJKSTRA_WITH_POTENTIALS);
    }

    private MinimumCostFlow solve(Graph graph, long requestedFlow, PathStrategy pathStrategy) {
        Objects.requireNonNull(graph, "graph must not be null");
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
        return new HashMap<>(bellmanFord(graph, residualGraph, source, true).distances());
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

                long reducedCost = arc.getCost()
                    + potentials.getOrDefault(arc.getFrom(), 0L)
                    - potentials.getOrDefault(arc.getTo(), 0L);
                if (reducedCost < 0) {
                    throw new IllegalStateException("Reduced cost must be non-negative.");
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
        BELLMAN_FORD,
        DIJKSTRA_WITH_POTENTIALS
    }

    private record ShortestPathTree(Map<Vertex, Long> distances, Map<Vertex, ResidualArc> parents) {
    }

    private record VertexDistance(Vertex vertex, long distance) {
    }
}
