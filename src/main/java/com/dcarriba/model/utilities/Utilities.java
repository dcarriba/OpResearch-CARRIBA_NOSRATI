package com.dcarriba.model.utilities;

import com.dcarriba.main.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Utilities {

    public static Path generatePdfFromDot(Path dotPath) throws IOException, InterruptedException {
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

    public static Path outputDirectoryFor(Path inputPath) {
        String inputFileName = inputPath.getFileName().toString();
        String inputName = removeExtension(inputFileName);

        return Config.OUTPUT_ROOT.resolve(inputName);
    }

    private static String removeExtension(String fileName) {
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart <= 0) {
            return fileName;
        }

        return fileName.substring(0, extensionStart);
    }
}
