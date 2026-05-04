package com.dcarriba.main;

import com.dcarriba.model.algorithm.FordFulkerson;
import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.GraphInputParser;
import com.dcarriba.model.graph.GraphDotSerializer;
import com.dcarriba.model.graph.Result;
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

            Result result = new FordFulkerson().solve(graph);
            Path resultOutputPath = Path.of("result.dot");
            new GraphDotSerializer().writeToFile(result, resultOutputPath);
            Path resultPdfOutputPath = Utilities.generatePdfFromDot(resultOutputPath);

            System.out.println("Graph successfully loaded from: " + inputPath);
            System.out.println("Vertices: " + graph.getNumberOfVertices());
            System.out.println("Arcs: " + graph.getNumberOfArcs());
            System.out.println("Source: " + graph.getSource().getId());
            System.out.println("Sink: " + graph.getSink().getId());
            System.out.println("DOT file of graph generated at: " + graphOutputPath);
            System.out.println("PDF file of graph generated at: " + graphPdfOutputPath);
            System.out.println("DOT file of result generated at: " + resultOutputPath);
            System.out.println("PDF file of result generated at: " + resultPdfOutputPath);
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
