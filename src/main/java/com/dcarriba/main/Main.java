package com.dcarriba.main;

import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.GraphInputParser;

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

            System.out.println("Graph successfully loaded from: " + inputPath);
            System.out.println("Vertices: " + graph.getNumberOfVertices());
            System.out.println("Arcs: " + graph.getNumberOfArcs());
            System.out.println("Source: " + graph.getSource().getId());
            System.out.println("Sink: " + graph.getSink().getId());
        } catch (IOException e) {
            System.err.println("Cannot read input file: " + inputPath);
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid graph input format in file: " + inputPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
