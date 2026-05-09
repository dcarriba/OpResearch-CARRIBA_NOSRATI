package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.MinimumCostFlow;
import com.dcarriba.model.graph.Vertex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MinimumCostFlowDotSerializer extends DotSerializer<MinimumCostFlow> {

    @Override
    public String serialize(MinimumCostFlow minimumCostFlow) {
        Objects.requireNonNull(minimumCostFlow, "minimumCostFlow must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2MinimumCostFlow{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("    label=\"flow = ")
            .append(minimumCostFlow.getFlowValue())
            .append(", minimum cost = ")
            .append(minimumCostFlow.getMinimumCost())
            .append("\"\n");
        dot.append("    labelloc=t\n");
        dot.append("\n");

        Vertex source = minimumCostFlow.getSource();
        Vertex sink = minimumCostFlow.getSink();

        for (Arc arc : sortedArcs(minimumCostFlow.getFlows().keySet(), source, sink)) {
            long flow = minimumCostFlow.getFlow(arc);
            String from = super.toDotNodeId(arc.getFrom());
            String to = super.toDotNodeId(arc.getTo());

            dot.append("    ")
                .append(from)
                .append(" -> ")
                .append(to)
                .append(" [label = <<font color=\"blue\">")
                .append(flow)
                .append("</font>/<font color=\"green\">")
                .append(arc.getMaximumCapacity())
                .append("</font>,<font color=\"red\">")
                .append(arc.getCost())
                .append("</font>>");

            if (flow > 0) {
                dot.append(",color=blue,penwidth=1.0");
            }

            dot.append("]\n");
        }

        dot.append("    ")
            .append(super.toDotNodeId(sink))
            .append(" -> ")
            .append(super.toDotNodeId(source))
            .append(" [color=red]\n");
        dot.append("\n");

        dot.append("    ")
            .append(super.toDotNodeId(source))
            .append(" [label=\"")
            .append(super.escapeForDot(source.getLabel()))
            .append("\",color=green]\n");

        List<Vertex> middleVertices = sortedVertices(minimumCostFlowVertices(minimumCostFlow), source, sink).stream()
            .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
            .toList();

        for (Vertex vertex : middleVertices) {
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

    @Override
    public void writeToFile(MinimumCostFlow minimumCostFlow, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Files.writeString(outputPath, serialize(minimumCostFlow), StandardCharsets.UTF_8);
    }

    private List<Arc> sortedArcs(Set<Arc> arcs, Vertex source, Vertex sink) {
        return arcs.stream()
            .sorted(Comparator.comparingInt((Arc arc) -> minimumCostFlowVertexRank(arc.getFrom(), source, sink))
                .thenComparingInt(arc -> arc.getFrom().getId())
                .thenComparingInt(arc -> minimumCostFlowVertexRank(arc.getTo(), source, sink))
                .thenComparingInt(arc -> arc.getTo().getId())
                .thenComparingInt(Arc::getMaximumCapacity)
                .thenComparingInt(Arc::getCost))
            .toList();
    }

    private Set<Vertex> minimumCostFlowVertices(MinimumCostFlow minimumCostFlow) {
        Set<Vertex> vertices = new LinkedHashSet<>();
        vertices.add(minimumCostFlow.getSource());
        vertices.add(minimumCostFlow.getSink());

        for (Arc arc : minimumCostFlow.getFlows().keySet()) {
            vertices.add(arc.getFrom());
            vertices.add(arc.getTo());
        }

        return vertices;
    }

    private List<Vertex> sortedVertices(Set<Vertex> vertices, Vertex source, Vertex sink) {
        return vertices.stream()
            .sorted(Comparator.comparingInt((Vertex vertex) -> minimumCostFlowVertexRank(vertex, source, sink))
                .thenComparingInt(Vertex::getId))
            .toList();
    }

    private int minimumCostFlowVertexRank(Vertex vertex, Vertex source, Vertex sink) {
        if (vertex.getId() == source.getId()) {
            return -1;
        }

        if (vertex.getId() == sink.getId()) {
            return 1;
        }

        return 0;
    }
}
