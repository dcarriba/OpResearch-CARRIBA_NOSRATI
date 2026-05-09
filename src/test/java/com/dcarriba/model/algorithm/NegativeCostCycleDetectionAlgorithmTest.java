package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.ResidualArc;
import com.dcarriba.model.graph.input.GraphInputParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NegativeCostCycleDetectionAlgorithmTest {

    private final NegativeCostCycleDetectionAlgorithm algorithm = new NegativeCostCycleDetectionAlgorithm();

    @Test
    void shouldReturnEmptyWhenGraphHasNoNegativeCostCycle() {
        Graph graph = new GraphInputParser().parse("""
                3 3 0 2
                0 1 1 2
                1 2 1 3
                0 2 1 10
                """);

        assertFalse(algorithm.hasNegativeCostCycle(graph));
        assertTrue(algorithm.findNegativeCostCycle(graph).isEmpty());
    }

    @Test
    void shouldFindNegativeCostCycle() {
        Graph graph = new GraphInputParser().parse("""
                3 3 0 2
                0 1 1 1
                1 2 1 -3
                2 0 1 1
                """);

        Optional<List<ResidualArc>> cycle = algorithm.findNegativeCostCycle(graph);

        assertTrue(cycle.isPresent());
        assertValidNegativeCycle(cycle.get());
    }

    @Test
    void shouldFindNegativeCostCycleNotReachableFromSource() {
        Graph graph = new GraphInputParser().parse("""
                4 3 0 1
                0 1 1 5
                2 3 1 -2
                3 2 1 1
                """);

        Optional<List<ResidualArc>> cycle = algorithm.findNegativeCostCycle(graph);

        assertTrue(cycle.isPresent());
        assertValidNegativeCycle(cycle.get());
    }

    private void assertValidNegativeCycle(List<ResidualArc> cycle) {
        assertFalse(cycle.isEmpty());
        assertTrue(cycle.stream().allMatch(arc -> arc.getResidualCapacity() > 0));
        assertTrue(cycle.stream().mapToLong(ResidualArc::getCost).sum() < 0);

        for (int i = 0; i < cycle.size(); i++) {
            ResidualArc current = cycle.get(i);
            ResidualArc next = cycle.get((i + 1) % cycle.size());
            assertSame(current.getTo(), next.getFrom());
        }

        assertEquals(cycle.getFirst().getFrom(), cycle.getLast().getTo());
    }
}
