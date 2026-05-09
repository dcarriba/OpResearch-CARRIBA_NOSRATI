package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.ResidualGraph;
import com.dcarriba.model.graph.input.GraphInputParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResidualGraphDotSerializerTest {

    @Test
    void shouldSerializeForwardAndReverseResidualArcsToDotFormat() {
        String input = """
                2 1 0 1
                0 1 3 7
                """;
        Graph graph = new GraphInputParser().parse(input);
        ResidualGraph residualGraph = new ResidualGraph(graph);

        String dot = new ResidualGraphDotSerializer().serialize(residualGraph);

        assertTrue(dot.contains("0 -> 1 [label = <<font color=\"green\">3</font>,<font color=\"red\">7</font>>,color=black]"));
        assertTrue(dot.contains("1 -> 0 [label = <<font color=\"green\">0</font>,<font color=\"red\">-7</font>>,color=black,style=dashed,fontcolor=gray]"));
        assertTrue(dot.contains("0 [label=\"0\",color=green]"));
        assertTrue(dot.contains("1 [label=\"1\",color=blue]"));
    }

    @Test
    void shouldSerializeArcsAndVerticesWithSourceFirstSinkLastAndMiddleVerticesOrderedById() {
        String input = """
                5 4 2 4
                2 3 1 0
                3 1 1 0
                1 0 1 0
                0 4 1 0
                """;
        Graph graph = new GraphInputParser().parse(input);
        ResidualGraph residualGraph = new ResidualGraph(graph);

        String dot = new ResidualGraphDotSerializer().serialize(residualGraph);

        assertTrue(dot.indexOf("2 -> 3")
            < dot.indexOf("0 -> 4"));
        assertTrue(dot.indexOf("0 -> 4")
            < dot.indexOf("1 -> 0"));
        assertTrue(dot.indexOf("1 -> 0")
            < dot.indexOf("3 -> 1"));
        assertTrue(dot.indexOf("3 -> 1")
            < dot.indexOf("4 -> 0"));
        assertTrue(dot.indexOf("2 [label=\"2\",color=green]")
            < dot.indexOf("0 [label=\"0\"]"));
        assertTrue(dot.indexOf("0 [label=\"0\"]")
            < dot.indexOf("1 [label=\"1\"]"));
        assertTrue(dot.indexOf("1 [label=\"1\"]")
            < dot.indexOf("3 [label=\"3\"]"));
        assertTrue(dot.indexOf("3 [label=\"3\"]")
            < dot.indexOf("4 [label=\"4\",color=blue]"));
    }

    @Test
    void shouldWriteResidualGraphDotFile() throws IOException {
        String input = """
                2 1 0 1
                0 1 3 7
                """;
        Graph graph = new GraphInputParser().parse(input);
        ResidualGraph residualGraph = new ResidualGraph(graph);
        ResidualGraphDotSerializer serializer = new ResidualGraphDotSerializer();

        Path output = Files.createTempFile("residualGraph-", ".dot");
        serializer.writeToFile(residualGraph, output);

        String generated = Files.readString(output, StandardCharsets.UTF_8);

        assertEquals(serializer.serialize(residualGraph), generated);
    }
}
