package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.MaxFlowMinCut;
import com.dcarriba.model.graph.Vertex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MaxFlowMinCutDotSerializer extends DotSerializer<MaxFlowMinCut> {

    @Override
    public String serialize(MaxFlowMinCut maxFlowMinCut) {
        Objects.requireNonNull(maxFlowMinCut, "maxFlowMinCut must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2MaxFlowMinCut{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("    label=\"maximum flow = ")
                .append(maxFlowMinCut.getMaximumFlow())
                .append(", minimum cut = ")
                .append(maxFlowMinCut.getMinimumCutCapacity())
                .append("\"\n");
        dot.append("    labelloc=t\n");
        dot.append("\n");

        Vertex source = maxFlowMinCut.getSource();
        Vertex sink = maxFlowMinCut.getSink();

        for (Arc arc : sortedArcs(maxFlowMinCut.getFlows().keySet(), source, sink)) {
            String from = super.toDotNodeId(arc.getFrom());
            String to = super.toDotNodeId(arc.getTo());

            dot.append("    ")
                    .append(from)
                    .append(" -> ")
                    .append(to)
                    .append(" [label = <<font color=\"blue\">")
                    .append(maxFlowMinCut.getFlow(arc))
                    .append("</font>/<font color=\"green\">")
                    .append(arc.getMaximumCapacity())
                    .append("</font>>");

            if (maxFlowMinCut.getMinimumCutArcs().contains(arc)) {
                dot.append(",color=orange,penwidth=2.0");
            } else {
                if (maxFlowMinCut.getFlow(arc) > 0) {
                    dot.append(",color=blue,penwidth=1.0");
                }
            }

            dot.append("]\n");
        }

        dot.append("    ")
                .append(super.toDotNodeId(maxFlowMinCut.getSink()))
                .append(" -> ")
                .append(super.toDotNodeId(maxFlowMinCut.getSource()))
                .append(" [color=red]\n");
        dot.append("\n");

        dot.append("    ")
                .append(super.toDotNodeId(source))
                .append(" [label=\"")
                .append(super.escapeForDot(source.getLabel()))
                .append("\",color=green]\n");

        List<Vertex> middleVertices = sortedVertices(maxFlowMinCutVertices(maxFlowMinCut), source, sink).stream()
                .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
                .toList();

        for (Vertex vertex : middleVertices) {
            dot.append("    ")
                    .append(super.toDotNodeId(vertex))
                    .append(" [label=\"")
                    .append(super.escapeForDot(vertex.getLabel()))
                    .append("\"");

            if (isIncidentToMinimumCutArc(vertex, maxFlowMinCut)) {
                dot.append(",style=filled,fillcolor=orange");
            }

            dot.append("]\n");
        }

        dot.append("    ")
                .append(super.toDotNodeId(sink))
                .append(" [label=\"")
                .append(super.escapeForDot(sink.getLabel()))
                .append("\",color=blue]\n");
        dot.append("}\n");

        return dot.toString();
    }

    @Override
    public void writeToFile(MaxFlowMinCut maxFlowMinCut, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Files.writeString(outputPath, serialize(maxFlowMinCut), StandardCharsets.UTF_8);
    }

    private List<Arc> sortedArcs(Set<Arc> arcs, Vertex source, Vertex sink) {
        return arcs.stream()
                .sorted(Comparator.comparingInt((Arc arc) -> maxFlowMinCutVertexRank(arc.getFrom(), source, sink))
                        .thenComparingInt(arc -> arc.getFrom().getId())
                        .thenComparingInt(arc -> maxFlowMinCutVertexRank(arc.getTo(), source, sink))
                        .thenComparingInt(arc -> arc.getTo().getId())
                        .thenComparingInt(Arc::getMaximumCapacity)
                        .thenComparingInt(Arc::getCost))
                .toList();
    }

    private Set<Vertex> maxFlowMinCutVertices(MaxFlowMinCut maxFlowMinCut) {
        Set<Vertex> vertices = new LinkedHashSet<>();
        vertices.addAll(maxFlowMinCut.getMinimumCutSourceSide());
        vertices.addAll(maxFlowMinCut.getMinimumCutSinkSide());
        return vertices;
    }

    private List<Vertex> sortedVertices(Set<Vertex> vertices, Vertex source, Vertex sink) {
        return vertices.stream()
                .sorted(Comparator.comparingInt((Vertex vertex) -> maxFlowMinCutVertexRank(vertex, source, sink))
                        .thenComparingInt(Vertex::getId))
                .toList();
    }

    private boolean isIncidentToMinimumCutArc(Vertex vertex, MaxFlowMinCut maxFlowMinCut) {
        return maxFlowMinCut.getMinimumCutArcs().stream()
                .anyMatch(arc -> arc.getFrom().equals(vertex) || arc.getTo().equals(vertex));
    }

    private int maxFlowMinCutVertexRank(Vertex vertex, Vertex source, Vertex sink) {
        if (vertex.getId() == source.getId()) {
            return -1;
        }

        if (vertex.getId() == sink.getId()) {
            return 1;
        }

        return 0;
    }
}
