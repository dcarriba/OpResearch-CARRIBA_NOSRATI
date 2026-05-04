package com.dcarriba.model.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ResidualGraph {
    private final Map<Vertex, List<ResidualArc>> adjacencyList = new LinkedHashMap<>();

    public ResidualGraph(Graph graph) {
        for (Vertex vertex : graph.getVertices()) {
            adjacencyList.put(vertex, new ArrayList<>());
        }

        for (Arc arc : graph.getArcs()) {
            if (arc.getMaximumCapacity() < 0) {
                throw new IllegalArgumentException("Arc capacity must be >= 0.");
            }

            adjacencyList.putIfAbsent(arc.getFrom(), new ArrayList<>());
            adjacencyList.putIfAbsent(arc.getTo(), new ArrayList<>());

            ResidualArc forward = new ResidualArc(arc.getFrom(), arc.getTo(), arc.getMaximumCapacity(), arc, true);
            ResidualArc reverse = new ResidualArc(arc.getTo(), arc.getFrom(), 0, arc, false);
            forward.setReverse(reverse);
            reverse.setReverse(forward);

            adjacencyList.get(arc.getFrom()).add(forward);
            adjacencyList.get(arc.getTo()).add(reverse);
        }
    }

    public List<ResidualArc> getArcsFrom(Vertex vertex) {
        return Collections.unmodifiableList(adjacencyList.getOrDefault(vertex, List.of()));
    }

    /*
     * Ford-Fulkerson is the general augmenting-path framework. This method uses
     * BFS to choose each path, which is the Edmonds-Karp path-selection variant.
     */
    public List<ResidualArc> findAugmentingPath(Vertex source, Vertex sink) {
        Queue<Vertex> queue = new ArrayDeque<>();
        Set<Vertex> visited = new LinkedHashSet<>();
        Map<Vertex, ResidualArc> parentEdges = new HashMap<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty() && !visited.contains(sink)) {
            Vertex current = queue.remove();

            for (ResidualArc edge : getArcsFrom(current)) {
                if (edge.getResidualCapacity() <= 0 || visited.contains(edge.getTo())) {
                    continue;
                }

                visited.add(edge.getTo());
                parentEdges.put(edge.getTo(), edge);
                queue.add(edge.getTo());
            }
        }

        if (!visited.contains(sink)) {
            return List.of();
        }

        List<ResidualArc> path = new ArrayList<>();
        Vertex current = sink;
        while (!current.equals(source)) {
            ResidualArc edge = parentEdges.get(current);
            path.add(edge);
            current = edge.getFrom();
        }

        Collections.reverse(path);
        return path;
    }

    public Set<Vertex> findReachableVertices(Vertex source) {
        Queue<Vertex> queue = new ArrayDeque<>();
        Set<Vertex> visited = new LinkedHashSet<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            Vertex current = queue.remove();

            for (ResidualArc edge : getArcsFrom(current)) {
                if (edge.getResidualCapacity() > 0 && visited.add(edge.getTo())) {
                    queue.add(edge.getTo());
                }
            }
        }

        return new LinkedHashSet<>(visited);
    }
}
