package com.dcarriba.model.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphInputParserTest {

    @Test
    void shouldParseGraphInputFormat() {
        String input = """
                6 10 5 4
                5 0 10 2
                5 1 8 4
                0 1 5 5
                0 2 5 2
                1 0 4 1
                1 3 10 4
                2 1 7 1
                2 3 6 2
                2 4 3 1
                3 4 14 3
                """;

        GraphInputParser parser = new GraphInputParser();
        Graph graph = parser.parse(input);

        assertEquals(6, graph.getNumberOfVertices());
        assertEquals(10, graph.getNumberOfArcs());
    }
}
