package com.originspecs.dataprep.config;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public record Config(
        Path inputFile,
        Path outputFile,
        int requiredCells
) {
    public static Config fromArgs(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments required: <inputFile> <outputFile> <requiredCells>");
        }

        var inputFile = Path.of(args[0]);
        var outputFile = Path.of(args[1]);
        var requiredCells = parseRequiredCells(args[2]);

        return new Config(inputFile, outputFile, requiredCells);
    }

    private static int parseRequiredCells(String arg) {
        try {
            int cells = Integer.parseInt(arg);
            if (cells <= 0) {
                throw new IllegalArgumentException("Required cells must be positive: " + cells);
            }
            return cells;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Required cells must be an integer: " + arg);
        }
    }

    public void validate() {
        if (!inputFile.toFile().exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + inputFile.toAbsolutePath());
        }

        var parentDir = outputFile.getParent();
        if (parentDir != null && !parentDir.toFile().exists()) {
            log.info("Output directory will be created: {}", parentDir.toAbsolutePath());
        }
    }
}