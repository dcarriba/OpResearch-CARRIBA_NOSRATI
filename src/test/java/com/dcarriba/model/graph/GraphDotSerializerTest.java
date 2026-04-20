package com.dcarriba.model.graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphDotSerializerTest {

    @Test
    void shouldSerializeGraphToDotFormat() {
        String input = """
                6 10 5 4
                5 0 40 2
                0 1 15 4
                0 2 8 4
                0 3 5 8
                1 2 20 2
                1 3 4 2
                1 4 10 6
                2 3 15 1
                2 4 4 3
                3 4 20 2
                """;

        Graph graph = new GraphInputParser().parse(input);
        graph.getVertex(0).setLabel("toto");

        String dot = new GraphDotSerializer().serialize(graph);

        String expected = """
                digraph Gv2{
                    graph [nodesep="0.3", ranksep="0.3",fontsize=12]
                    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]
                    edge [arrowsize=0.6]

                    5 -> 0 [label = <<font color="green">40</font>,<font color="red">2</font>>]
                    0 -> 1 [label = <<font color="green">15</font>,<font color="red">4</font>>]
                    0 -> 2 [label = <<font color="green">8</font>,<font color="red">4</font>>]
                    0 -> 3 [label = <<font color="green">5</font>,<font color="red">8</font>>]
                    1 -> 2 [label = <<font color="green">20</font>,<font color="red">2</font>>]
                    1 -> 3 [label = <<font color="green">4</font>,<font color="red">2</font>>]
                    1 -> 4 [label = <<font color="green">10</font>,<font color="red">6</font>>]
                    2 -> 3 [label = <<font color="green">15</font>,<font color="red">1</font>>]
                    2 -> 4 [label = <<font color="green">4</font>,<font color="red">3</font>>]
                    3 -> 4 [label = <<font color="green">20</font>,<font color="red">2</font>>]
                    4 -> 5 [color=red]

                    5 [label="5",color=green]
                    0 [label="toto"]
                    1 [label="1"]
                    2 [label="2"]
                    3 [label="3"]
                    4 [label="4",color=blue]
                }
                """;

        assertEquals(expected, dot);
    }

    @Test
    void shouldWriteDotFile() throws IOException {
        String input = """
                2 1 0 1
                0 1 3 7
                """;

        Graph graph = new GraphInputParser().parse(input);
        GraphDotSerializer serializer = new GraphDotSerializer();

        Path output = Files.createTempFile("graph-", ".dot");
        serializer.writeToFile(graph, output);

        String generated = Files.readString(output, StandardCharsets.UTF_8);
        String expected = serializer.serialize(graph);

        assertEquals(expected, generated);
    }
}
