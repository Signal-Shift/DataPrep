package com.originspecs.dataprep.orchestration;

import com.originspecs.dataprep.config.Config;
import com.originspecs.dataprep.processor.ColumnThresholdProcessor;
import com.originspecs.dataprep.reader.RowMappers;
import com.originspecs.dataprep.reader.XlsFileReader;
import com.originspecs.dataprep.writer.ListStringXlsWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Orchestrates the complete data preparation pipeline: read → process → write
 */
@Slf4j
public class DataPrepOrchestrator {

    private final XlsFileReader<List<String>> reader;
    private final ColumnThresholdProcessor processor;
    private final ListStringXlsWriter writer;

    public DataPrepOrchestrator() {
        this(new XlsFileReader<>(), new ColumnThresholdProcessor(), new ListStringXlsWriter());
    }

    public DataPrepOrchestrator(XlsFileReader<List<String>> reader,
                                ColumnThresholdProcessor processor,
                                ListStringXlsWriter writer) {
        this.reader = reader;
        this.processor = processor;
        this.writer = writer;
    }

    /**
     * Executes the complete data preparation pipeline.
     *
     * @param config Configuration containing input/output paths and processing parameters
     * @throws IOException if reading or writing fails
     */
    public void execute(Config config) throws IOException {
        log.info("Starting data preparation pipeline");
        log.info("Input file: {}", config.inputFile());
        log.info("Output file: {}", config.outputFile());
        log.info("Column threshold: {}", config.columnThreshold());

        List<List<String>> rows = read(config.inputFile());
        List<List<String>> processedRows = process(rows, config.columnThreshold());
        write(processedRows, config.outputFile());

        log.info("Data preparation pipeline completed successfully");
    }

    private List<List<String>> read(java.nio.file.Path inputFile) throws IOException {
        log.debug("Reading XLS file");
        return reader.readXlsFileIntoList(inputFile, RowMappers.toStringList());
    }

    private List<List<String>> process(List<List<String>> rows, double columnThreshold) {
        log.debug("Processing rows with column threshold: {}", columnThreshold);
        return processor.process(rows, columnThreshold);
    }

    private void write(List<List<String>> rows, java.nio.file.Path outputFile) throws IOException {
        log.debug("Writing processed rows to output file");
        writer.write(rows, outputFile);
    }
}
