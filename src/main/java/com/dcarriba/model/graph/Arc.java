package com.dcarriba.model.graph;

public class Arc {
    private final Vertex from;
    private final Vertex to;
    private final int maximumCapacity;
    private final int cost;

    public Arc(Vertex from, Vertex to, int maximumCapacity, int cost) {
        this.from = from;
        this.to = to;
        this.maximumCapacity = maximumCapacity;
        this.cost = cost;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public int getCost() {
        return cost;
    }
}
