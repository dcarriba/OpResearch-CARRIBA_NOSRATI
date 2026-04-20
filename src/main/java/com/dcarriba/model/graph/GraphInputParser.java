package com.dcarriba.model.graph;

import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class GraphInputParser {

    public Graph parse(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return parse(new StringReader(input));
    }

    public Graph parse(Readable readable) {
        Objects.requireNonNull(readable, "readable must not be null");
        Scanner scanner = new Scanner(readable);

        int numberOfVertices = readNextInt(scanner, "number of vertices");
        int numberOfArcs = readNextInt(scanner, "number of arcs");
        int sourceId = readNextInt(scanner, "source id");
        int sinkId = readNextInt(scanner, "sink id");

        validateHeader(numberOfVertices, numberOfArcs, sourceId, sinkId);

        Set<Vertex> vertices = new LinkedHashSet<>();
        for (int i = 0; i < numberOfVertices; i++) {
            vertices.add(new Vertex(i));
        }

        Set<Arc> arcs = new LinkedHashSet<>();
        for (int i = 0; i < numberOfArcs; i++) {
            int fromId = readNextInt(scanner, "arc " + i + " from vertex id");
            int toId = readNextInt(scanner, "arc " + i + " to vertex id");
            int maximumCapacity = readNextInt(scanner, "arc " + i + " maximum capacity");
            int cost = readNextInt(scanner, "arc " + i + " cost");

            Vertex from = findVertexById(vertices, fromId);
            Vertex to = findVertexById(vertices, toId);

            if (maximumCapacity < 0) {
                throw new IllegalArgumentException("Arc " + i + " capacity must be >= 0.");
            }

            if (cost < 0) {
                throw new IllegalArgumentException("Arc " + i + " cost must be >= 0.");
            }

            arcs.add(new Arc(from, to, maximumCapacity, cost));
        }

        Vertex source = findVertexById(vertices, sourceId);
        Vertex sink = findVertexById(vertices, sinkId);

        return new Graph(arcs, vertices, source, sink);
    }

    private void validateHeader(int numberOfVertices, int numberOfArcs, int sourceId, int sinkId) {
        if (numberOfVertices <= 0) {
            throw new IllegalArgumentException("Number of nodes must be > 0.");
        }

        if (numberOfArcs < 0) {
            throw new IllegalArgumentException("Number of arcs must be >= 0.");
        }

        if (sourceId < 0 || sourceId >= numberOfVertices) {
            throw new IllegalArgumentException("Source id must be between 0 and " + (numberOfVertices - 1) + ".");
        }

        if (sinkId < 0 || sinkId >= numberOfVertices) {
            throw new IllegalArgumentException("Sink id must be between 0 and " + (numberOfVertices - 1) + ".");
        }
    }

    private int readNextInt(Scanner scanner, String fieldName) {
        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Missing or invalid integer for " + fieldName + ".");
        }
        return scanner.nextInt();
    }

    private Vertex findVertexById(Set<Vertex> vertices, int vertexId) {
        for (Vertex vertex : vertices) {
            if (vertex.getId() == vertexId) {
                return vertex;
            }
        }

        throw new IllegalArgumentException("Unknown vertex id=" + vertexId + ".");
    }
}
