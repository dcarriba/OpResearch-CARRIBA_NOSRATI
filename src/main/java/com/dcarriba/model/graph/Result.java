package com.dcarriba.model.graph;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Result {
    private final Vertex source;
    private final Vertex sink;
    private final long maximumFlow;
    private final long minimumCutCapacity;
    private final Set<Vertex> minimumCutSourceSide;
    private final Set<Vertex> minimumCutSinkSide;
    private final Set<Arc> minimumCutArcs;
    private final Map<Arc, Long> flows;

    public Result(
        Vertex source,
        Vertex sink,
        long maximumFlow,
        long minimumCutCapacity,
        Set<Vertex> minimumCutSourceSide,
        Set<Vertex> minimumCutSinkSide,
        Set<Arc> minimumCutArcs,
        Map<Arc, Long> flows
    ) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
        this.maximumFlow = maximumFlow;
        this.minimumCutCapacity = minimumCutCapacity;
        this.minimumCutSourceSide = Set.copyOf(minimumCutSourceSide);
        this.minimumCutSinkSide = Set.copyOf(minimumCutSinkSide);
        this.minimumCutArcs = Set.copyOf(minimumCutArcs);
        this.flows = Map.copyOf(flows);
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getSink() {
        return sink;
    }

    public long getMaximumFlow() {
        return maximumFlow;
    }

    public long getMinimumCutCapacity() {
        return minimumCutCapacity;
    }

    public Set<Vertex> getMinimumCutSourceSide() {
        return minimumCutSourceSide;
    }

    public Set<Vertex> getMinimumCutSinkSide() {
        return minimumCutSinkSide;
    }

    public Set<Arc> getMinimumCutArcs() {
        return minimumCutArcs;
    }

    public Map<Arc, Long> getFlows() {
        return flows;
    }

    public long getFlow(Arc arc) {
        Objects.requireNonNull(arc, "arc must not be null");
        return flows.getOrDefault(arc, 0L);
    }
}
