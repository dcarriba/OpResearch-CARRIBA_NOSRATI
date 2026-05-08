package com.dcarriba.main;

import com.dcarriba.model.algorithm.FordFulkerson;
import com.dcarriba.model.algorithm.MinimumCostFlowAlgorithm;
import com.dcarriba.model.graph.*;
import com.dcarriba.model.graph.dot.GraphDotSerializer;
import com.dcarriba.model.graph.dot.MaxFlowMinCutDotSerializer;
import com.dcarriba.model.graph.dot.MinimumCostFlowDotSerializer;
import com.dcarriba.model.graph.input.GraphInputParser;
import com.dcarriba.model.utilities.Utilities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The {@link Main} class of the project.
 */
public class Main {

    /**
     * Main function of the project
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ./gradlew run --args=\"path/to/input.txt\"");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);

        try {
            String input = Files.readString(inputPath, StandardCharsets.UTF_8);

            Graph graph = new GraphInputParser().parse(input);
            Path graphOutputPath = Path.of("graph.dot");
            new GraphDotSerializer().writeToFile(graph, graphOutputPath);
            Path graphPdfOutputPath = Utilities.generatePdfFromDot(graphOutputPath);

            MaxFlowMinCut maxFlowMinCut = new FordFulkerson().solve(graph);
            Path maxFlowMinCutOutputPath = Path.of("maxFlowMinCut.dot");
            new MaxFlowMinCutDotSerializer().writeToFile(maxFlowMinCut, maxFlowMinCutOutputPath);
            Path maxFlowMinCutPdfOutputPath = Utilities.generatePdfFromDot(maxFlowMinCutOutputPath);

            MinimumCostFlow minimumCostFlowWithPathAlgorithm = new MinimumCostFlowAlgorithm().solveWithPathAlgorithm(graph);
            Path minimumCostFlowWithPathAlgorithmOutputPath = Path.of("minimumCostFlowWithPathAlgorithm.dot");
            new MinimumCostFlowDotSerializer().writeToFile(minimumCostFlowWithPathAlgorithm, minimumCostFlowWithPathAlgorithmOutputPath);
            Path minimumCostFlowWithPathAlgorithmPdfOutputPath = Utilities.generatePdfFromDot(minimumCostFlowWithPathAlgorithmOutputPath);

            MinimumCostFlow minimumCostFlowWithDijkstraAndCostNormalization = new MinimumCostFlowAlgorithm().solveWithDijkstraAndCostNormalization(graph);
            Path minimumCostFlowWithDijkstraAndCostNormalizationOutputPath = Path.of("minimumCostFlowWithDijkstraAndCostNormalization.dot");
            new MinimumCostFlowDotSerializer().writeToFile(minimumCostFlowWithDijkstraAndCostNormalization, minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);
            Path minimumCostFlowWithDijkstraAndCostNormalizationPdfOutputPath = Utilities.generatePdfFromDot(minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);

            System.out.println("Graph successfully loaded from: " + inputPath);
            System.out.println("Vertices: " + graph.getNumberOfVertices());
            System.out.println("Arcs: " + graph.getNumberOfArcs());
            System.out.println("Source: " + graph.getSource().getId());
            System.out.println("Sink: " + graph.getSink().getId());
            System.out.println("DOT file of graph generated at: " + graphOutputPath);
            System.out.println("PDF file of graph generated at: " + graphPdfOutputPath);
            System.out.println("DOT file of maxFlowMinCut generated at: " + maxFlowMinCutOutputPath);
            System.out.println("PDF file of maxFlowMinCut generated at: " + maxFlowMinCutPdfOutputPath);
            System.out.println("DOT file of minimumCostFlowWithPathAlgorithm generated at: " + minimumCostFlowWithPathAlgorithmOutputPath);
            System.out.println("PDF file of minimumCostFlowWithPathAlgorithm generated at: " + minimumCostFlowWithPathAlgorithmPdfOutputPath);
            System.out.println("DOT file of minimumCostFlowWithDijkstraAndCostNormalization generated at: " + minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);
            System.out.println("PDF file of minimumCostFlowWithDijkstraAndCostNormalization generated at: " + minimumCostFlowWithDijkstraAndCostNormalizationPdfOutputPath);
        } catch (IOException e) {
            System.err.println("I/O error while processing graph files.");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while generating PDF from DOT file.");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid graph input format in file: " + inputPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
