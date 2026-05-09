package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.ResidualArc;
import com.dcarriba.model.graph.ResidualGraph;
import com.dcarriba.model.graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class NegativeCostCycleDetectionAlgorithm {

    public boolean hasNegativeCostCycle(Graph graph) {
        return findNegativeCostCycle(graph).isPresent();
    }

    public Optional<List<ResidualArc>> findNegativeCostCycle(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");
        return findNegativeCostCycle(graph, new ResidualGraph(graph));
    }

    public Optional<List<ResidualArc>> findNegativeCostCycle(Graph graph, ResidualGraph residualGraph) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(residualGraph, "residualGraph must not be null");

        Map<Vertex, Long> distances = new HashMap<>();
        Map<Vertex, ResidualArc> parents = new HashMap<>();

        for (Vertex vertex : graph.getVertices()) {
            distances.put(vertex, 0L);
        }

        Vertex updatedVertex = null;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            updatedVertex = null;

            for (Vertex from : graph.getVertices()) {
                long distanceFrom = distances.get(from);

                for (ResidualArc arc : residualGraph.getArcsFrom(from)) {
                    if (arc.getResidualCapacity() <= 0) {
                        continue;
                    }

                    long candidate = distanceFrom + arc.getCost();
                    if (candidate < distances.get(arc.getTo())) {
                        distances.put(arc.getTo(), candidate);
                        parents.put(arc.getTo(), arc);
                        updatedVertex = arc.getTo();
                    }
                }
            }
        }

        if (updatedVertex == null) {
            return Optional.empty();
        }

        return Optional.of(buildCycle(updatedVertex, parents, graph.getNumberOfVertices()));
    }

    private List<ResidualArc> buildCycle(
        Vertex updatedVertex,
        Map<Vertex, ResidualArc> parents,
        int numberOfVertices
    ) {
        Vertex cycleVertex = updatedVertex;
        for (int i = 0; i < numberOfVertices; i++) {
            cycleVertex = parents.get(cycleVertex).getFrom();
        }

        List<ResidualArc> cycle = new ArrayList<>();
        Vertex current = cycleVertex;
        do {
            ResidualArc arc = parents.get(current);
            cycle.add(arc);
            current = arc.getFrom();
        } while (!current.equals(cycleVertex));

        Collections.reverse(cycle);
        return cycle;
    }
}
