package com.originspecs.dataprep.processor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes rows by filtering out columns that don't meet the minimum fill threshold.
 */
@Slf4j
public class ColumnThresholdProcessor {

    /**
     * Filters columns from rows based on the minimum fill percentage threshold.
     * Columns with fill percentage below the threshold are removed.
     *
     * @param rows The rows to process (each row is a List<String> of cell values)
     * @param columnThreshold Minimum fill percentage (0.0 to 1.0) required to keep a column
     * @return Processed rows with filtered columns
     */
    public List<List<String>> process(List<List<String>> rows, double columnThreshold) {
        if (rows.isEmpty()) {
            log.warn("No rows to process");
            return rows;
        }

        List<Integer> columnsToKeep = determineColumnsToKeep(rows, columnThreshold);
        return filterColumns(rows, columnsToKeep);
    }

    private List<Integer> determineColumnsToKeep(List<List<String>> rows, double threshold) {
        int maxColumns = findMaxColumnCount(rows);
        if (maxColumns == 0) {
            return new ArrayList<>();
        }

        int totalRows = rows.size();
        List<Integer> columnsToKeep = new ArrayList<>();

        for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
            int nonEmptyCount = countNonEmptyCells(rows, colIndex);
            double fillPercentage = (double) nonEmptyCount / totalRows;

            if (fillPercentage >= threshold) {
                columnsToKeep.add(colIndex);
                log.debug("Keeping column {}: {}% fill", colIndex, String.format("%.1f", fillPercentage * 100));
            } else {
                log.info("Removing column {}: {}% fill (below {}% threshold)",
                        colIndex, String.format("%.1f", fillPercentage * 100), String.format("%.1f", threshold * 100));
            }
        }

        log.info("Keeping {} out of {} columns", columnsToKeep.size(), maxColumns);
        return columnsToKeep;
    }

    private int findMaxColumnCount(List<List<String>> rows) {
        return rows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
    }

    private int countNonEmptyCells(List<List<String>> rows, int columnIndex) {
        int count = 0;
        for (List<String> row : rows) {
            if (columnIndex < row.size()) {
                String value = row.get(columnIndex);
                if (value != null && !value.trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    private List<List<String>> filterColumns(List<List<String>> rows, List<Integer> columnsToKeep) {
        List<List<String>> filtered = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            List<String> filteredRow = new ArrayList<>(columnsToKeep.size());
            for (Integer colIndex : columnsToKeep) {
                if (colIndex < row.size()) {
                    filteredRow.add(row.get(colIndex));
                } else {
                    filteredRow.add("");
                }
            }
            filtered.add(filteredRow);
        }
        return filtered;
    }
}
