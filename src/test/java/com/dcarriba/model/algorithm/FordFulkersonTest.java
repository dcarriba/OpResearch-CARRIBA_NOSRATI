package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.GraphInputParser;
import com.dcarriba.model.graph.Result;
import com.dcarriba.model.graph.Vertex;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FordFulkersonTest {

    @Test
    void shouldComputeMaximumFlowAndMinimumCutForClassicalNetwork() {
        String input = """
                6 10 0 5
                0 1 16 0
                0 2 13 0
                1 2 10 0
                2 1 4 0
                1 3 12 0
                3 2 9 0
                2 4 14 0
                4 3 7 0
                3 5 20 0
                4 5 4 0
                """;
        Graph graph = new GraphInputParser().parse(input);

        Result result = new FordFulkerson().solve(graph);

        assertEquals(23, result.getMaximumFlow());
        assertEquals(23, result.getMinimumCutCapacity());
        assertVertexIds(Set.of(0, 1, 2, 4), result.getMinimumCutSourceSide());
        assertVertexIds(Set.of(3, 5), result.getMinimumCutSinkSide());
        assertArcIds(Set.of("1->3", "4->3", "4->5"), result.getMinimumCutArcs());
    }

    @Test
    void shouldSupportZeroCapacityAndParallelArcsWithoutMutatingGraph() {
        String input = """
                3 4 0 2
                0 1 5 0
                0 1 0 0
                0 1 7 0
                1 2 10 0
                """;
        Graph graph = new GraphInputParser().parse(input);
        Map<String, Integer> capacitiesBefore = graph.getArcs().stream()
            .collect(Collectors.toMap(this::arcKeyWithCapacity, Arc::getMaximumCapacity, (left, right) -> left));

        Result result = new FordFulkerson().solve(graph);

        assertEquals(10, result.getMaximumFlow());
        assertEquals(10, result.getMinimumCutCapacity());
        assertArcIds(Set.of("1->2"), result.getMinimumCutArcs());
        assertEquals(4, graph.getNumberOfArcs());
        assertEquals(capacitiesBefore, graph.getArcs().stream()
            .collect(Collectors.toMap(this::arcKeyWithCapacity, Arc::getMaximumCapacity, (left, right) -> left)));
    }

    @Test
    void shouldTreatVerticesWithSameIdAsSameGraphVertex() {
        Vertex source = new Vertex(0);
        Vertex middle = new Vertex(1);
        Vertex sink = new Vertex(2);

        Set<Vertex> vertices = Set.of(source, middle, sink);
        Set<Arc> arcs = Set.of(
            new Arc(new Vertex(0), new Vertex(1), 5, 0),
            new Arc(new Vertex(1), new Vertex(2), 3, 0)
        );
        Graph graph = new Graph(arcs, vertices, source, sink);

        Result result = new FordFulkerson().solve(graph);

        assertEquals(3, result.getMaximumFlow());
        assertEquals(3, result.getMinimumCutCapacity());
    }

    private void assertVertexIds(Set<Integer> expectedIds, Set<com.dcarriba.model.graph.Vertex> vertices) {
        Set<Integer> actualIds = vertices.stream()
            .map(com.dcarriba.model.graph.Vertex::getId)
            .collect(Collectors.toSet());

        assertEquals(expectedIds, actualIds);
    }

    private void assertArcIds(Set<String> expectedIds, Set<Arc> arcs) {
        Set<String> actualIds = arcs.stream()
            .map(arc -> arc.getFrom().getId() + "->" + arc.getTo().getId())
            .collect(Collectors.toSet());

        assertEquals(expectedIds, actualIds);
    }

    private String arcKeyWithCapacity(Arc arc) {
        return arc.getFrom().getId() + "->" + arc.getTo().getId() + ":" + arc.getMaximumCapacity();
    }
}
