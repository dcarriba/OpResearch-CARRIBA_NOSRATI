package com.dcarriba.model.graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GraphDotSerializer {

    public String serialize(Graph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("\n");

        for (Arc arc : graph.getArcs()) {
            String from = toDotNodeId(arc.getFrom());
            String to = toDotNodeId(arc.getTo());

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
            .append(toDotNodeId(graph.getSink()))
            .append(" -> ")
            .append(toDotNodeId(graph.getSource()))
            .append(" [color=red]\n");
        dot.append("\n");

        Vertex source = graph.getSource();
        Vertex sink = graph.getSink();

        dot.append("    ")
            .append(toDotNodeId(source))
            .append(" [label=\"")
            .append(escapeForDot(source.getLabel()))
            .append("\",color=green]\n");

        List<Vertex> middleVertices = graph.getVertices().stream()
            .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
            .sorted(Comparator.comparingInt(Vertex::getId))
            .toList();

        for (Vertex vertex : middleVertices) {
            dot.append("    ")
                .append(toDotNodeId(vertex))
                .append(" [label=\"")
                .append(escapeForDot(vertex.getLabel()))
                .append("\"]\n");
        }

        dot.append("    ")
            .append(toDotNodeId(sink))
            .append(" [label=\"")
            .append(escapeForDot(sink.getLabel()))
            .append("\",color=blue]\n");
        dot.append("}\n");

        return dot.toString();
    }

    public void writeToFile(Graph graph, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Files.writeString(outputPath, serialize(graph), StandardCharsets.UTF_8);
    }

    public String serialize(Result result) {
        Objects.requireNonNull(result, "result must not be null");

        StringBuilder dot = new StringBuilder();

        dot.append("digraph Gv2Result{\n");
        dot.append("    graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        dot.append("    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        dot.append("    edge [arrowsize=0.6]\n");
        dot.append("    label=\"maximum flow = ")
            .append(result.getMaximumFlow())
            .append(", minimum cut = ")
            .append(result.getMinimumCutCapacity())
            .append("\"\n");
        dot.append("    labelloc=t\n");
        dot.append("\n");

        for (Arc arc : sortedArcs(result.getFlows().keySet())) {
            String from = toDotNodeId(arc.getFrom());
            String to = toDotNodeId(arc.getTo());

            dot.append("    ")
                .append(from)
                .append(" -> ")
                .append(to)
                .append(" [label = <<font color=\"blue\">")
                .append(result.getFlow(arc))
                .append("</font>/<font color=\"green\">")
                .append(arc.getMaximumCapacity())
                .append("</font>>");

            if (result.getMinimumCutArcs().contains(arc)) {
                dot.append(",color=blue,penwidth=2.0");
            }

            dot.append("]\n");
        }

        dot.append("    ")
            .append(toDotNodeId(result.getSink()))
            .append(" -> ")
            .append(toDotNodeId(result.getSource()))
            .append(" [color=red]\n");
        dot.append("\n");

        Vertex source = result.getSource();
        Vertex sink = result.getSink();

        dot.append("    ")
            .append(toDotNodeId(source))
            .append(" [label=\"")
            .append(escapeForDot(source.getLabel()))
            .append("\",color=green]\n");

        List<Vertex> middleVertices = sortedVertices(resultVertices(result)).stream()
            .filter(vertex -> vertex.getId() != source.getId() && vertex.getId() != sink.getId())
            .toList();

        for (Vertex vertex : middleVertices) {
            dot.append("    ")
                .append(toDotNodeId(vertex))
                .append(" [label=\"")
                .append(escapeForDot(vertex.getLabel()))
                .append("\"");

            if (result.getMinimumCutSourceSide().contains(vertex)) {
                dot.append(",style=filled,fillcolor=palegreen");
            } else if (result.getMinimumCutSinkSide().contains(vertex)) {
                dot.append(",style=filled,fillcolor=lightblue");
            }

            dot.append("]\n");
        }

        dot.append("    ")
            .append(toDotNodeId(sink))
            .append(" [label=\"")
            .append(escapeForDot(sink.getLabel()))
            .append("\",color=blue]\n");
        dot.append("}\n");

        return dot.toString();
    }

    public void writeToFile(Result result, Path outputPath) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Files.writeString(outputPath, serialize(result), StandardCharsets.UTF_8);
    }

    private List<Arc> sortedArcs(Set<Arc> arcs) {
        return arcs.stream()
            .sorted(Comparator.comparingInt((Arc arc) -> arc.getFrom().getId())
                .thenComparingInt(arc -> arc.getTo().getId())
                .thenComparingInt(Arc::getMaximumCapacity)
                .thenComparingInt(Arc::getCost))
            .toList();
    }

    private Set<Vertex> resultVertices(Result result) {
        Set<Vertex> vertices = new LinkedHashSet<>();
        vertices.addAll(result.getMinimumCutSourceSide());
        vertices.addAll(result.getMinimumCutSinkSide());
        return vertices;
    }

    private List<Vertex> sortedVertices(Set<Vertex> vertices) {
        return vertices.stream()
            .sorted(Comparator.comparingInt(Vertex::getId))
            .toList();
    }

    private String toDotNodeId(Vertex vertex) {
        return Integer.toString(vertex.getId());
    }

    private String escapeForDot(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
