package com.originspecs.specextractor;

import com.originspecs.specextractor.model.Employee;
import com.originspecs.specextractor.model.Vehicle;
import com.originspecs.specextractor.processor.DataProcessor;
import com.originspecs.specextractor.reader.FileReader;
import com.originspecs.specextractor.writer.JsonFileWriter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {

        if (args.length != 3) {
            log.error("Insufficient arguments. Expected 3, got {}", args.length);
            log.info("Usage: java -jar spec-extractor.jar <inputFile.xls> <outputFile.json> <quantity_of_cells_to_process");
            log.info("Real example with DEBUG level logs: ");
            log.info("java -DLOG_LEVEL=DEBUG -jar target/spec-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar \\\n" +
                    "                         src/main/resources/staff.xls output.json 7");

            System.exit(1);
        }

        var inputFile = args[0];
        var outputFile = args[1];
        var requiredCellsArg = args[2];

        int requiredCells = Integer.parseInt(requiredCellsArg);
        if (requiredCells <= 0) {
            log.error("Required cells must be a positive integer");
            System.exit(1);
        }


        log.info("Input file: {}", inputFile);
        log.info("Output file: {}", outputFile);

        try {

            // Read XLS file
            log.debug("=== Reading XLS file ===");
            var fileReader = new FileReader();
            List<Vehicle> vehicles = fileReader.readXls(inputFile);
            log.info("Read {} list size from XLS", vehicles.size());

            if (vehicles.isEmpty()) {
                log.warn("No employees were parsed from the XLS file");
            }

            // Convert to JSON and write to file
            log.debug("=== Writing JSON output ===");
            var outputPath = Path.of(outputFile);
            var jsonWriter = new JsonFileWriter();
            jsonWriter.write(vehicles, outputPath);

            log.info("Processing completed successfully");

        } catch (Exception e) {
            log.error("Failed to process files", e);
            System.exit(1);
        }
    }
}