package com.originspecs.dataprep.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating common RowMapper implementations.
 */
public class RowMappers {

    /**
     * Creates a RowMapper that converts a POI Row into a List<String> of cell values.
     * Empty cells are represented as empty strings.
     */
    public static RowMapper<List<String>> toStringList() {
        return row -> {
            int lastCellNum = row.getLastCellNum();
            List<String> cells = new ArrayList<>(Math.max(lastCellNum, 0));
            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = row.getCell(i);
                cells.add(cell == null ? "" : cell.toString());
            }
            return cells;
        };
    }
}
