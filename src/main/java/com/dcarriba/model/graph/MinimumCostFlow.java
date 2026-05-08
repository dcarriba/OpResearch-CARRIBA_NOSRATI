package com.dcarriba.model.graph;

import com.dcarriba.model.graph.dot.DotSerializable;

import java.util.Map;
import java.util.Objects;

public class MinimumCostFlow implements DotSerializable {
    private final Vertex source;
    private final Vertex sink;
    private final long flowValue;
    private final long minimumCost;
    private final Map<Arc, Long> flows;

    public MinimumCostFlow(Vertex source, Vertex sink, long flowValue, long minimumCost, Map<Arc, Long> flows) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
        this.flowValue = flowValue;
        this.minimumCost = minimumCost;
        this.flows = Map.copyOf(flows);
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getSink() {
        return sink;
    }

    public long getFlowValue() {
        return flowValue;
    }

    public long getMinimumCost() {
        return minimumCost;
    }

    public Map<Arc, Long> getFlows() {
        return flows;
    }

    public long getFlow(Arc arc) {
        Objects.requireNonNull(arc, "arc must not be null");
        return flows.getOrDefault(arc, 0L);
    }
}
