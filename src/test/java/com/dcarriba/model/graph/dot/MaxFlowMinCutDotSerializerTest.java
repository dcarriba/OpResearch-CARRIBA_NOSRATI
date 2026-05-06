package com.dcarriba.model.graph.dot;

import com.dcarriba.model.algorithm.FordFulkerson;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.MaxFlowMinCut;
import com.dcarriba.model.graph.input.GraphInputParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxFlowMinCutDotSerializerTest {

    @Test
    void shouldSerializeMaxFlowMinCutToDotFormat() {
        String input = """
                4 5 0 3
                0 1 3 0
                0 2 2 0
                1 2 1 0
                1 3 2 0
                2 3 4 0
                """;

        Graph graph = new GraphInputParser().parse(input);
        MaxFlowMinCut maxFlowMinCut = new FordFulkerson().solve(graph);

        String dot = new MaxFlowMinCutDotSerializer().serialize(maxFlowMinCut);

        String expected = """
                digraph Gv2MaxFlowMinCut{
                    graph [nodesep="0.3", ranksep="0.3",fontsize=12]
                    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]
                    edge [arrowsize=0.6]
                    label="maximum flow = 5, minimum cut = 5"
                    labelloc=t

                    0 -> 1 [label = <<font color="blue">3</font>/<font color="green">3</font>>,color=blue,penwidth=2.0]
                    0 -> 2 [label = <<font color="blue">2</font>/<font color="green">2</font>>,color=blue,penwidth=2.0]
                    1 -> 2 [label = <<font color="blue">1</font>/<font color="green">1</font>>]
                    1 -> 3 [label = <<font color="blue">2</font>/<font color="green">2</font>>]
                    2 -> 3 [label = <<font color="blue">3</font>/<font color="green">4</font>>]
                    3 -> 0 [color=red]

                    0 [label="0",color=green]
                    1 [label="1",style=filled,fillcolor=lightblue]
                    2 [label="2",style=filled,fillcolor=lightblue]
                    3 [label="3",color=blue]
                }
                """;

        assertEquals(expected, dot);
    }

    @Test
    void shouldWriteMaxFlowMinCutDotFile() throws IOException {
        String input = """
                2 1 0 1
                0 1 3 0
                """;

        Graph graph = new GraphInputParser().parse(input);
        MaxFlowMinCut maxFlowMinCut = new FordFulkerson().solve(graph);
        MaxFlowMinCutDotSerializer serializer = new MaxFlowMinCutDotSerializer();

        Path output = Files.createTempFile("maxFlowMinCut-", ".dot");
        serializer.writeToFile(maxFlowMinCut, output);

        String generated = Files.readString(output, StandardCharsets.UTF_8);
        String expected = serializer.serialize(maxFlowMinCut);

        assertEquals(expected, generated);
    }
}
