package com.originspecs.dataprep.writer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Adapter for XlsWriter to work with List<List<String>> data.
 * Each inner List<String> represents a row, where each String is a cell value.
 */
@Slf4j
public class ListStringXlsWriter {

    private final XlsWriter<List<String>> writer;

    public ListStringXlsWriter() {
        this.writer = new XlsWriter<>();
    }

    public ListStringXlsWriter(XlsWriter<List<String>> writer) {
        this.writer = writer;
    }

    /**
     * Writes rows (List<List<String>>) to an XLS file.
     * Each inner List<String> becomes a row, with each String becoming a cell.
     *
     * @param rows The rows to write
     * @param outputPath Where to save the file
     * @throws IOException if writing fails
     */
    public void write(List<List<String>> rows, Path outputPath) throws IOException {
        if (rows.isEmpty()) {
            log.warn("No rows to write");
            return;
        }

        int maxColumns = rows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        List<Function<List<String>, Object>> extractors = createExtractors(maxColumns);
        writer.write(rows, outputPath, extractors);
    }

    private List<Function<List<String>, Object>> createExtractors(int columnCount) {
        List<Function<List<String>, Object>> extractors = new java.util.ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            final int columnIndex = i;
            extractors.add(row -> {
                if (columnIndex < row.size()) {
                    String value = row.get(columnIndex);
                    return value != null ? value : "";
                }
                return "";
            });
        }
        return extractors;
    }
}
