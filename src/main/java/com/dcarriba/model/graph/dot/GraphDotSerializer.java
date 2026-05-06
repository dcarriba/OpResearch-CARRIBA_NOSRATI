package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.Vertex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GraphDotSerializer extends DotSerializer<Graph> {

    @Override
    public String serialize(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("\n");

        for (Arc arc : graph.getArcs()) {
            String from = super.toDotNodeId(arc.getFrom());
            String to = super.toDotNodeId(arc.getTo());

            dot.append("    ")
                .append(from)
                .append(" -> ")
                .append(to)
                .append(" [label = <<font color=\"green\">")
                .append(arc.getMaximumCapacity())
                .append("</font>,<font color=\"red\">")
                .append(arc.getCost())
                .append("</font>>]\n");
        }

        dot.append("    ")
            .append(super.toDotNodeId(graph.getSink()))
            .append(" -> ")
            .append(super.toDotNodeId(graph.getSource()))
            .append(" [color=red]\n");
        dot.append("\n");

        Vertex source = graph.getSource();
        Vertex sink = graph.getSink();

        dot.append("    ")
            .append(super.toDotNodeId(source))
            .append(" [label=\"")
            .append(super.escapeForDot(source.getLabel()))
            .append("\",color=green]\n");

        List<Vertex> middleVertices = graph.getVertices().stream()
            .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
            .sorted(Comparator.comparingInt(Vertex::getId))
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
    public void writeToFile(Graph graph, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Files.writeString(outputPath, serialize(graph), StandardCharsets.UTF_8);
    }
}
