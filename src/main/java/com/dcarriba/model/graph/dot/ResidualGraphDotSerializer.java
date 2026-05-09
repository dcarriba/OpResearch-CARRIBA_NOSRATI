package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.ResidualArc;
import com.dcarriba.model.graph.ResidualGraph;
import com.dcarriba.model.graph.Vertex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResidualGraphDotSerializer extends DotSerializer<ResidualGraph> {

    @Override
    public String serialize(ResidualGraph residualGraph) {
        Objects.requireNonNull(residualGraph, "residualGraph must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2ResidualGraph{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("    label=\"residual graph\"\n");
        dot.append("    labelloc=t\n");
        dot.append("\n");

        List<Vertex> orderedVertices = orderedVertices(residualGraph);
        Map<Vertex, Integer> vertexOrder = vertexOrder(orderedVertices);

        List<ResidualArc> orderedArcs = residualGraph.getArcs().stream()
            .sorted(Comparator
                .comparingInt((ResidualArc arc) -> vertexOrder.getOrDefault(arc.getFrom(), Integer.MAX_VALUE))
                .thenComparingInt(arc -> vertexOrder.getOrDefault(arc.getTo(), Integer.MAX_VALUE))
                .thenComparingInt(arc -> arc.isForward() ? 0 : 1)
                .thenComparingLong(ResidualArc::getResidualCapacity)
                .thenComparingInt(ResidualArc::getCost))
            .toList();

        for (ResidualArc arc : orderedArcs) {
            String from = super.toDotNodeId(arc.getFrom());
            String to = super.toDotNodeId(arc.getTo());

            dot.append("    ")
                .append(from)
                .append(" -> ")
                .append(to)
                .append(" [label = <<font color=\"green\">")
                .append(arc.getResidualCapacity())
                .append("</font>,<font color=\"red\">")
                .append(arc.getCost())
                .append("</font>>");

            if (arc.isForward()) {
                dot.append(",color=black");
            } else {
                dot.append(",color=black,style=dashed");
            }

            if (arc.getResidualCapacity() == 0) {
                dot.append(",fontcolor=gray");
            }

            dot.append("]\n");
        }

        Vertex source = orderedVertices.getFirst();
        Vertex sink = orderedVertices.getLast();
        dot.append("    ")
            .append(super.toDotNodeId(source))
            .append(" [label=\"")
            .append(super.escapeForDot(source.getLabel()))
            .append("\",color=green]\n");

        for (int i = 1; i < orderedVertices.size() - 1; i++) {
            Vertex vertex = orderedVertices.get(i);
            dot.append("    ")
                .append(super.toDotNodeId(vertex))
                .append(" [label=\"")
                .append(super.escapeForDot(vertex.getLabel()))
                .append("\"]\n");
        }

        dot.append("    ")
            .append(super.toDotNodeId(sink))
            .append(" [label=\"")
            .append(super.escapeForDot(sink.getLabel()))
            .append("\",color=blue]\n");

        dot.append("}\n");

        return dot.toString();
    }

    private List<Vertex> orderedVertices(ResidualGraph residualGraph) {
        Vertex source = residualGraph.getSource();
        Vertex sink = residualGraph.getSink();

        List<Vertex> middleVertices = residualGraph.getVertices().stream()
            .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
            .sorted(Comparator.comparingInt(Vertex::getId))
            .toList();

        return java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(java.util.stream.Stream.of(source), middleVertices.stream()),
                java.util.stream.Stream.of(sink)
            )
            .toList();
    }

    private Map<Vertex, Integer> vertexOrder(List<Vertex> orderedVertices) {
        Map<Vertex, Integer> vertexOrder = new HashMap<>();

        for (int i = 0; i < orderedVertices.size(); i++) {
            vertexOrder.put(orderedVertices.get(i), i);
        }

        return vertexOrder;
    }

    @Override
    public void writeToFile(ResidualGraph residualGraph, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, serialize(residualGraph), StandardCharsets.UTF_8);
    }
}
