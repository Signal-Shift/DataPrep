package com.originspecs.dataprep.config;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public record Config(
        Path inputFile,
        Path outputFile,
        double columnThreshold
) {
    public static Config fromArgs(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments required: <inputFile> <outputFile> <columnThreshold>");
        }

        var inputFile = Path.of(args[0]);
        var outputFile = Path.of(args[1]);
        var columnThreshold = parseColumnThreshold(args[2]);

        return new Config(inputFile, outputFile, columnThreshold);
    }

    private static double parseColumnThreshold(String arg) {
        try {
            double value = Double.parseDouble(arg);
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0 || value > 1) {
                throw new IllegalArgumentException("columnThreshold must be between 0.0 and 1.0, got: " + value);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("columnThreshold must be a number between 0.0 and 1.0: " + arg);
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