package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.ResidualArc;
import com.dcarriba.model.graph.ResidualGraph;
import com.dcarriba.model.graph.MaxFlowMinCut;
import com.dcarriba.model.graph.Vertex;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FordFulkerson {

    public MaxFlowMinCut solve(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        Vertex source = Objects.requireNonNull(graph.getSource(), "graph source must not be null");
        Vertex sink = Objects.requireNonNull(graph.getSink(), "graph sink must not be null");

        if (source.equals(sink)) {
            throw new IllegalArgumentException("Source and sink must be different vertices.");
        }

        ResidualGraph residualGraph = new ResidualGraph(graph);
        Map<Arc, Long> flows = new IdentityHashMap<>();
        for (Arc arc : graph.getArcs()) {
            flows.put(arc, 0L);
        }

        long maximumFlow = 0;
        List<ResidualArc> augmentingPath = residualGraph.findAugmentingPath(source, sink);

        while (!augmentingPath.isEmpty()) {
            long bottleneck = augmentingPath.stream()
                .mapToLong(ResidualArc::getResidualCapacity)
                .min()
                .orElseThrow();

            for (ResidualArc arc : augmentingPath) {
                arc.augment(bottleneck);

                if (arc.getOriginalArc() != null) {
                    flows.merge(
                        arc.getOriginalArc(),
                        arc.isForward() ? bottleneck : -bottleneck,
                        Long::sum
                    );
                }
            }

            maximumFlow += bottleneck;
            augmentingPath = residualGraph.findAugmentingPath(source, sink);
        }

        Set<Vertex> sourceSide = residualGraph.findReachableVertices(source);
        Set<Vertex> sinkSide = new LinkedHashSet<>(graph.getVertices());
        sinkSide.removeAll(sourceSide);

        Set<Arc> cutArcs = new LinkedHashSet<>();
        long cutCapacity = 0;
        for (Arc arc : graph.getArcs()) {
            if (sourceSide.contains(arc.getFrom()) && sinkSide.contains(arc.getTo())) {
                cutArcs.add(arc);
                cutCapacity += arc.getMaximumCapacity();
            }
        }

        return new MaxFlowMinCut(source, sink, maximumFlow, cutCapacity, sourceSide, sinkSide, cutArcs, flows);
    }
}
