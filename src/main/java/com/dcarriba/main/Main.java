package com.dcarriba.main;

import com.dcarriba.model.algorithm.FordFulkerson;
import com.dcarriba.model.algorithm.MinimumCostFlowAlgorithm;
import com.dcarriba.model.algorithm.NegativeCostCycleDetectionAlgorithm;
import com.dcarriba.model.algorithm.ResidualGraphObserver;
import com.dcarriba.model.graph.*;
import com.dcarriba.model.graph.dot.GraphDotSerializer;
import com.dcarriba.model.graph.dot.MaxFlowMinCutDotSerializer;
import com.dcarriba.model.graph.dot.MinimumCostFlowDotSerializer;
import com.dcarriba.model.graph.dot.ResidualGraphDotSerializer;
import com.dcarriba.model.graph.input.GraphInputParser;
import com.dcarriba.model.utilities.Utilities;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
            Path outputDir = Utilities.outputDirectoryFor(inputPath);
            Files.createDirectories(outputDir);

            Graph graph = new GraphInputParser().parse(input);
            boolean hasNegativeCostCycle = new NegativeCostCycleDetectionAlgorithm().hasNegativeCostCycle(graph);
            Path residualGraphsOutputDir = outputDir.resolve("residualGraphs");
            List<Path> residualGraphDotPaths = new ArrayList<>();
            ResidualGraphObserver residualGraphObserver = residualGraphFileObserver(
                residualGraphsOutputDir,
                residualGraphDotPaths
            );

            Path graphOutputPath = outputDir.resolve("graph.dot");
            new GraphDotSerializer().writeToFile(graph, graphOutputPath);
            Path graphPdfOutputPath = Utilities.generatePdfFromDot(graphOutputPath);

            MaxFlowMinCut maxFlowMinCut = new FordFulkerson().solve(graph, residualGraphObserver);
            Path maxFlowMinCutOutputPath = outputDir.resolve("maxFlowMinCut.dot");
            new MaxFlowMinCutDotSerializer().writeToFile(maxFlowMinCut, maxFlowMinCutOutputPath);
            Path maxFlowMinCutPdfOutputPath = Utilities.generatePdfFromDot(maxFlowMinCutOutputPath);

            MinimumCostFlow minimumCostFlowWithPathAlgorithm = new MinimumCostFlowAlgorithm()
                .solveWithPathAlgorithm(graph, residualGraphObserver);
            Path minimumCostFlowWithPathAlgorithmOutputPath = outputDir.resolve("minimumCostFlowWithPathAlgorithm.dot");
            new MinimumCostFlowDotSerializer().writeToFile(minimumCostFlowWithPathAlgorithm, minimumCostFlowWithPathAlgorithmOutputPath);
            Path minimumCostFlowWithPathAlgorithmPdfOutputPath = Utilities.generatePdfFromDot(minimumCostFlowWithPathAlgorithmOutputPath);

            MinimumCostFlow minimumCostFlowWithDijkstraAndCostNormalization = new MinimumCostFlowAlgorithm()
                .solveWithDijkstraAndCostNormalization(graph, residualGraphObserver);
            Path minimumCostFlowWithDijkstraAndCostNormalizationOutputPath = outputDir.resolve("minimumCostFlowWithDijkstraAndCostNormalization.dot");
            new MinimumCostFlowDotSerializer().writeToFile(minimumCostFlowWithDijkstraAndCostNormalization, minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);
            Path minimumCostFlowWithDijkstraAndCostNormalizationPdfOutputPath = Utilities.generatePdfFromDot(minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);
            List<Path> residualGraphPdfPaths = generatePdfsFromDotFiles(residualGraphDotPaths);

            System.out.println("Graph successfully loaded from: " + inputPath);
            System.out.println("Vertices: " + graph.getNumberOfVertices());
            System.out.println("Arcs: " + graph.getNumberOfArcs());
            System.out.println("Source: " + graph.getSource().getId());
            System.out.println("Sink: " + graph.getSink().getId());
            System.out.println("Has negative cost cycle: " + hasNegativeCostCycle);
            System.out.println("DOT file of graph generated at: " + graphOutputPath);
            System.out.println("PDF file of graph generated at: " + graphPdfOutputPath);
            System.out.println("DOT file of maxFlowMinCut generated at: " + maxFlowMinCutOutputPath);
            System.out.println("PDF file of maxFlowMinCut generated at: " + maxFlowMinCutPdfOutputPath);
            System.out.println("DOT file of minimumCostFlowWithPathAlgorithm generated at: " + minimumCostFlowWithPathAlgorithmOutputPath);
            System.out.println("PDF file of minimumCostFlowWithPathAlgorithm generated at: " + minimumCostFlowWithPathAlgorithmPdfOutputPath);
            System.out.println("DOT file of minimumCostFlowWithDijkstraAndCostNormalization generated at: " + minimumCostFlowWithDijkstraAndCostNormalizationOutputPath);
            System.out.println("PDF file of minimumCostFlowWithDijkstraAndCostNormalization generated at: " + minimumCostFlowWithDijkstraAndCostNormalizationPdfOutputPath);
            System.out.println("DOT and PDF files of all intermediary residual graphs generated in: " + residualGraphsOutputDir);
            System.out.println("Intermediary residual graphs DOT files generated: " + residualGraphDotPaths.size());
            System.out.println("Intermediary residual graphs PDF files generated: " + residualGraphPdfPaths.size());
        } catch (IOException e) {
            System.err.println("I/O error while processing graph files.");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (UncheckedIOException e) {
            System.err.println("I/O error while writing residual graph files.");
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

    private static ResidualGraphObserver residualGraphFileObserver(
        Path outputDir,
        List<Path> residualGraphDotPaths
    ) {
        ResidualGraphDotSerializer serializer = new ResidualGraphDotSerializer();

        return (algorithmName, step, residualGraph) -> {
            Path outputPath = outputDir.resolve(algorithmName + "-residual-" + step + ".dot");
            try {
                serializer.writeToFile(residualGraph, outputPath);
                residualGraphDotPaths.add(outputPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static List<Path> generatePdfsFromDotFiles(List<Path> dotPaths) throws IOException, InterruptedException {
        List<Path> pdfPaths = new ArrayList<>();

        for (Path dotPath : dotPaths) {
            pdfPaths.add(Utilities.generatePdfFromDot(dotPath));
        }

        return pdfPaths;
    }
}
