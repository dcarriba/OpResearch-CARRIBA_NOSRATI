package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Arc;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.MinimumCostFlow;
import com.dcarriba.model.graph.input.GraphInputParser;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimumCostFlowAlgorithmTest {

    @Test
    void shouldComputeMaximumFlowWithMinimumCostUsingNegativeCostPathAlgorithm() {
        Graph graph = graphWithNegativeCostArc();

        MinimumCostFlow minimumCostFlow = new MinimumCostFlowAlgorithm().solveWithPathAlgorithm(graph);

        assertEquals(3, minimumCostFlow.getFlowValue());
        assertEquals(10, minimumCostFlow.getMinimumCost());
        assertExpectedFlows(graph, minimumCostFlow);
    }

    @Test
    void shouldComputeMaximumFlowWithMinimumCostUsingDijkstraAndCostNormalization() {
        Graph graph = graphWithNegativeCostArc();

        MinimumCostFlow minimumCostFlow = new MinimumCostFlowAlgorithm().solveWithDijkstraAndCostNormalization(graph);

        assertEquals(3, minimumCostFlow.getFlowValue());
        assertEquals(10, minimumCostFlow.getMinimumCost());
        assertExpectedFlows(graph, minimumCostFlow);
    }

    @Test
    void shouldLimitFlowToRequestedAmount() {
        Graph graph = graphWithNegativeCostArc();

        MinimumCostFlow minimumCostFlow = new MinimumCostFlowAlgorithm().solveWithPathAlgorithm(graph, 1);

        assertEquals(1, minimumCostFlow.getFlowValue());
        assertEquals(0, minimumCostFlow.getMinimumCost());
        assertEquals(1, minimumCostFlow.getFlow(arcById(graph).get("1->2")));
    }

    @Test
    void shouldRejectRequestedFlowGreaterThanNetworkCapacity() {
        Graph graph = graphWithNegativeCostArc();

        assertThrows(
            IllegalArgumentException.class,
            () -> new MinimumCostFlowAlgorithm().solveWithDijkstraAndCostNormalization(graph, 4)
        );
    }

    private Graph graphWithNegativeCostArc() {
        String input = """
                4 5 0 3
                0 1 2 2
                0 2 2 4
                1 2 1 -3
                1 3 1 3
                2 3 2 1
                """;

        return new GraphInputParser().parse(input);
    }

    private void assertExpectedFlows(Graph graph, MinimumCostFlow minimumCostFlow) {
        Map<String, Arc> arcsById = arcById(graph);

        assertEquals(2, minimumCostFlow.getFlow(arcsById.get("0->1")));
        assertEquals(1, minimumCostFlow.getFlow(arcsById.get("0->2")));
        assertEquals(1, minimumCostFlow.getFlow(arcsById.get("1->2")));
        assertEquals(1, minimumCostFlow.getFlow(arcsById.get("1->3")));
        assertEquals(2, minimumCostFlow.getFlow(arcsById.get("2->3")));
    }

    private Map<String, Arc> arcById(Graph graph) {
        return graph.getArcs().stream()
            .collect(Collectors.toMap(arc -> arc.getFrom().getId() + "->" + arc.getTo().getId(), Function.identity()));
    }
}
