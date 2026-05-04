package com.dcarriba.model.graph;

public class ResidualArc {
    private final Vertex from;
    private final Vertex to;
    private final Arc originalArc;
    private final boolean forward;
    private long residualCapacity;
    private ResidualArc reverse;

    public ResidualArc(Vertex from, Vertex to, long residualCapacity, Arc originalArc, boolean forward) {
        this.from = from;
        this.to = to;
        this.residualCapacity = residualCapacity;
        this.originalArc = originalArc;
        this.forward = forward;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public long getResidualCapacity() {
        return residualCapacity;
    }

    public void setResidualCapacity(long residualCapacity) {
        this.residualCapacity = residualCapacity;
    }

    public Arc getOriginalArc() {
        return originalArc;
    }

    public boolean isForward() {
        return forward;
    }

    public void setReverse(ResidualArc reverse) {
        this.reverse = reverse;
    }

    public void augment(long flow) {
        residualCapacity -= flow;
        reverse.setResidualCapacity(reverse.getResidualCapacity() + flow);
    }
}
