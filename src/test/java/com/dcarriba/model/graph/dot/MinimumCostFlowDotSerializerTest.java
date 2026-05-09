package com.dcarriba.model.graph.dot;

import com.dcarriba.model.algorithm.MinimumCostFlowAlgorithm;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.MinimumCostFlow;
import com.dcarriba.model.graph.input.GraphInputParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinimumCostFlowDotSerializerTest {

    @Test
    void shouldSerializeMinimumCostFlowToDotFormat() {
        String input = """
                4 5 0 3
                0 1 2 2
                0 2 2 4
                1 2 1 -3
                1 3 1 3
                2 3 2 1
                """;

        Graph graph = new GraphInputParser().parse(input);
        MinimumCostFlow minimumCostFlow = new MinimumCostFlowAlgorithm().solveWithPathAlgorithm(graph);

        String dot = new MinimumCostFlowDotSerializer().serialize(minimumCostFlow);

        String expected = """
                digraph Gv2MinimumCostFlow{
                    graph [nodesep="0.3", ranksep="0.3",fontsize=12]
                    node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]
                    edge [arrowsize=0.6]
                    label="flow = 3, minimum cost = 10"
                    labelloc=t

                    0 -> 1 [label = <<font color="blue">2</font>/<font color="green">2</font>,<font color="red">2</font>>,color=blue,penwidth=1.0]
                    0 -> 2 [label = <<font color="blue">1</font>/<font color="green">2</font>,<font color="red">4</font>>,color=blue,penwidth=1.0]
                    1 -> 2 [label = <<font color="blue">1</font>/<font color="green">1</font>,<font color="red">-3</font>>,color=blue,penwidth=1.0]
                    1 -> 3 [label = <<font color="blue">1</font>/<font color="green">1</font>,<font color="red">3</font>>,color=blue,penwidth=1.0]
                    2 -> 3 [label = <<font color="blue">2</font>/<font color="green">2</font>,<font color="red">1</font>>,color=blue,penwidth=1.0]
                    3 -> 0 [color=red]

                    0 [label="0",color=green]
                    1 [label="1"]
                    2 [label="2"]
                    3 [label="3",color=blue]
                }
                """;

        assertEquals(expected, dot);
    }

    @Test
    void shouldWriteMinimumCostFlowDotFile() throws IOException {
        String input = """
                2 1 0 1
                0 1 3 7
                """;

        Graph graph = new GraphInputParser().parse(input);
        MinimumCostFlow minimumCostFlow = new MinimumCostFlowAlgorithm().solveWithPathAlgorithm(graph);
        MinimumCostFlowDotSerializer serializer = new MinimumCostFlowDotSerializer();

        Path output = Files.createTempFile("minimumCostFlow-", ".dot");
        serializer.writeToFile(minimumCostFlow, output);

        String generated = Files.readString(output, StandardCharsets.UTF_8);
        String expected = serializer.serialize(minimumCostFlow);

        assertEquals(expected, generated);
    }
}
