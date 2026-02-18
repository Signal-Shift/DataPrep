package com.originspecs.dataprep.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes POI Sheet objects to remove empty columns based on fill threshold.
 * This class works with Apache POI Sheet objects directly.
 */
@Slf4j
public class Processor {

    /**
     * Removes empty columns from a sheet in memory, skipping header rows
     * @param sheet The sheet to clean
     * @param threshold Minimum percentage of non-empty cells (0.0 to 1.0)
     * @param headerRows Number of header rows to skip in analysis
     * @return List of column indices that were kept
     */


    public List<Integer> removeEmptyColumnsFromSheet(Sheet sheet, double threshold, int headerRows) {
        DataFormatter formatter = new DataFormatter();
        int lastColumn = findLastColumn(sheet);
        int totalRows = sheet.getLastRowNum();

        // Don't count header rows in the analysis
        int dataRows = totalRows - headerRows;
        if (dataRows <= 0) {
            log.warn("No data rows found after skipping {} header rows", headerRows);
            return new ArrayList<>();
        }

        log.info("Analyzing {} data rows (skipping {} header rows)", dataRows, headerRows);

        // Analyze which columns to keep
        List<Integer> columnsToKeep = new ArrayList<>();

        for (int colIndex = 0; colIndex <= lastColumn; colIndex++) {
            int nonEmptyCount = 0;

            // Skip header rows when counting
            for (int rowIndex = headerRows; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        String value = formatter.formatCellValue(cell).trim();
                        if (!value.isEmpty()) {
                            nonEmptyCount++;
                        }
                    }
                }
            }

            double fillPercentage = (double) nonEmptyCount / dataRows;

            if (fillPercentage >= threshold) {
                columnsToKeep.add(colIndex);
                log.debug("Keeping column {}: {}% fill",
                        colIndex, String.format("%.1f", fillPercentage * 100));
            } else {
                log.info("Removing column {}: {}% fill (below {}% threshold)",
                        colIndex, String.format("%.1f", fillPercentage * 100),
                        String.format("%.1f", threshold * 100));
            }
        }

        log.info("Keeping {} out of {} columns", columnsToKeep.size(), lastColumn + 1);
        return columnsToKeep;
    }

    public List<Integer> removeEmptyColumnsFromSheet(Sheet sheet, double threshold) {
        // Default to 3 header rows based on your file structure
        return removeEmptyColumnsFromSheet(sheet, threshold, 3);
    }

    private int findLastColumn(Sheet sheet) {
        int lastColumn = -1;
        for (Row row : sheet) {
            int rowLastCell = row.getLastCellNum() - 1;
            if (rowLastCell > lastColumn) {
                lastColumn = rowLastCell;
            }
        }
        return lastColumn;
    }

}