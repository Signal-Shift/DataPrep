package com.originspecs.dataprep.writer;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class XlsWriter<T> {

    /**
     * Writes records to an .xls file.
     *
     * @param records         The data to write.
     * @param outputPath      Where to save the file.
     * @param valueExtractors A list of functions to extract data from T for each column.
     */

    public void write(List<T> records, Path outputPath, List<Function<T, Object>> valueExtractors) throws IOException {
        log.debug("Writing {} records to xls file", records.size());
        log.debug("Output path: {}", outputPath.toAbsolutePath());

        // Check if output directory exists and is writable
        var parentDir = outputPath.getParent();
        if (parentDir != null) {
            if (!Files.exists(parentDir)) {
                log.info("Creating parent directory: {}", parentDir);
                Files.createDirectories(parentDir);
            }
        }

        //Create a new workbook and sheet
        try (Workbook workbook = new HSSFWorkbook();
             OutputStream os = Files.newOutputStream(outputPath)) {

            var sheet = workbook.createSheet("Data");
            populateSheet(sheet, records, valueExtractors);

            workbook.write(os);
            log.info("Successfully wrote XLS file");

        }
    }

    // Handles writing all rows to the sheet
    private void populateSheet(Sheet sheet, List<T> records, List<Function<T, Object>> valueExtractors) {
        int rowIndex = 0;

        for (T record : records) {
            Row row = sheet.createRow(rowIndex++);
            int colIndex = 0;
            for (Function<T, Object> extractor : valueExtractors) {
                Object value = extractor.apply(record);
                setCellValue(row.createCell(colIndex++), value);
            }
        }
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null){
            return;
        }

        switch (value) {
            case String s -> cell.setCellValue(s);
            case Number n -> cell.setCellValue(n.doubleValue());
            case Boolean b -> cell.setCellValue(b);
            default -> cell.setCellValue(value.toString());

        }
    }
}