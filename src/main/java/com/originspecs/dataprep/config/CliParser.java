package com.originspecs.dataprep.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CliParser {

    private static final String USAGE = """
            Usage: java -jar DataPrep.jar <inputFile.xls> <outputFile.xls> <columnThreshold>
            columnThreshold: Value between 0.0 and 1.0 (e.g. 0.1 = 10%% minimum fill to keep column)
            Example: java -jar target/DataPrep.jar nissan.xls output.xls 0.1
            """;

    /**
     * Parses CLI arguments into a validated Config, or logs error, prints usage and exits the process.
     */
    public static Config parseOrExit(String[] args) {

        try {
            Config config = Config.fromArgs(args);
            config.validate();
            return config;
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments: {}", e.getMessage());
            log.error(USAGE);
            System.exit(1);
            return null;
        }
    }
}
