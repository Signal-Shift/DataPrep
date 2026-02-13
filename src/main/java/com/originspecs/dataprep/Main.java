package com.originspecs.dataprep;

import com.originspecs.dataprep.reader.RowMapper;
import com.originspecs.dataprep.reader.XlsFileReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {

    public enum RecordType {
        VEHICLE
    }

    public static void main(String[] args) {

        double columnThreshold = 0.1; // Default 10%

        if (args.length < 3) {
            log.error("Insufficient arguments. Expected at least 3, got {}", args.length);
            log.info("Usage: java -jar DataPrep.jar <inputFile.xls> <outputFile.xls> [columnThreshold]");
            log.info("columnThreshold: Optional, default 0.1 (10% minimum fill to keep column)");
            log.info("java -jar target/DataPrep.jar nissan.xls output.xls 20");
            System.exit(1);
        }

        try {
            String inputFile = args[0];
            String outputFile = args[1];

            columnThreshold = Double.parseDouble(args[2]);
            if (columnThreshold < 0 || columnThreshold > 1) {
                log.error("columnThreshold must be between 0.0 and 1.0");
                System.exit(1);
            }

            log.info("Input file: {}", inputFile);

            // Map each POI Row to a List<String> of cell values (generic row type)
            RowMapper<List<String>> rowMapper = row -> {
                int lastCellNum = row.getLastCellNum();
                List<String> cells = new ArrayList<>(lastCellNum);
                for (int i = 0; i < lastCellNum; i++) {
                    Cell cell = row.getCell(i);
                    cells.add(cell == null ? "" : cell.toString());
                }
                return cells;
            };

            XlsFileReader<List<String>> fileReader = new XlsFileReader<>();
            List<List<String>> rows = fileReader.readXlsFileIntoList(Path.of(inputFile), rowMapper);

            // #TEMP logging to debug

            int rowIndex = 0;
            for (List<String> row : rows) {
                log.info("Row {}: {}", rowIndex++, row);
            }

            // TODO: process rows (e.g. column threshold), then write modified xls via XlsWriter


        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }
}