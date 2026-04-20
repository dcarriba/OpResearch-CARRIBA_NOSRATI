package com.dcarriba.main;

import com.dcarriba.model.graph.Graph;
import com.dcarriba.model.graph.GraphInputParser;
import com.dcarriba.model.graph.GraphDotSerializer;

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
            Path outputPath = Path.of("graph.dot");
            new GraphDotSerializer().writeToFile(graph, outputPath);
            Path pdfOutputPath = generatePdfFromDot(outputPath);

            System.out.println("Graph successfully loaded from: " + inputPath);
            System.out.println("Vertices: " + graph.getNumberOfVertices());
            System.out.println("Arcs: " + graph.getNumberOfArcs());
            System.out.println("Source: " + graph.getSource().getId());
            System.out.println("Sink: " + graph.getSink().getId());
            System.out.println("DOT file generated at: " + outputPath);
            System.out.println("PDF file generated at: " + pdfOutputPath);
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

    private static Path generatePdfFromDot(Path dotPath) throws IOException, InterruptedException {
        Path parent = dotPath.getParent();
        String fileName = dotPath.getFileName().toString();
        String pdfFileName = fileName + ".pdf";
        Path pdfPath;
        if (parent == null) {
            pdfPath = Path.of(pdfFileName);
        } else {
            pdfPath = parent.resolve(pdfFileName);
        }

        Process process = new ProcessBuilder("dot", "-Tpdf", dotPath.toString(), "-o", pdfPath.toString())
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMessage;
            if (output.isEmpty()) {
                errorMessage = "Graphviz 'dot' command failed with exit code " + exitCode + ".";
            } else {
                errorMessage = output;
            }
            throw new IOException(errorMessage);
        }

        return pdfPath;
    }
}
