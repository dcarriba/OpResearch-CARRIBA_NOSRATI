package com.dcarriba.model.graph;

import com.dcarriba.model.graph.dot.DotSerializable;

import java.util.Set;

public class Graph implements DotSerializable {
    private final Set<Arc> arcs;
    private final Set<Vertex> vertices;
    private Vertex source;
    private Vertex sink;

    public Graph(Set<Arc> arcs, Set<Vertex> vertices, Vertex source, Vertex sink) {
        this.arcs = arcs;
        this.vertices = vertices;
        this.source = source;
        this.sink = sink;
    }

    public Set<Arc> getArcs() {
        return arcs;
    }

    public int getNumberOfArcs() {
        return arcs.size();
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public int getNumberOfVertices() {
        return vertices.size();
    }

    public Vertex getVertex(int id) {
        for (Vertex vertex : vertices) {
            if (vertex.getId() == id) {
                return vertex;
            }
        }

        throw new IllegalArgumentException("Vertex with id=" + id + "does not exist.");
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getSink() {
        return sink;
    }

    public Vertex changeSourceTo(int vertexId) {
        this.source = getVertex(vertexId);
        return this.source;
    }

    public Vertex changeSinkTo(int vertexId) {
        this.sink = getVertex(vertexId);
        return this.sink;
    }
}
